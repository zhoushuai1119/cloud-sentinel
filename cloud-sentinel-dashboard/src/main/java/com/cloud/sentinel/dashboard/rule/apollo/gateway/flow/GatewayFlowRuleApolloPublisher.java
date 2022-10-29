/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.sentinel.dashboard.rule.apollo.gateway.flow;

import com.cloud.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.cloud.sentinel.dashboard.rule.DynamicRulePublisher;
import com.cloud.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 01398395
 * @since 1.5.0
 */
@Component("gatewayFlowRuleApolloPublisher")
public class GatewayFlowRuleApolloPublisher implements DynamicRulePublisher<List<GatewayFlowRuleEntity>> {

    private final Logger logger = LoggerFactory.getLogger(GatewayFlowRuleApolloPublisher.class);

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<List<GatewayFlowRuleEntity>, String> converter;
    @Value("${app.id}")
    private String appId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${apollo.user}")
    private String user;
    @Value("${apollo.clusterName}")
    private String clusterName;
    @Value("${apollo.gateway.namespaceName}")
    private String gatewayNamespaceName;

    @Override
    public void publish(String app, List<GatewayFlowRuleEntity> rules) {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        filterField(rules);
        // Increase the configuration
        String flowDataId = ApolloConfigUtil.getGatewayFlowDataId(app);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(converter.convert(rules));
        openItemDTO.setComment(app + "网关流控规则");
        openItemDTO.setDataChangeCreatedBy(user);
        apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, gatewayNamespaceName, openItemDTO);

        // 发布配置
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        namespaceReleaseDTO.setReleasedBy(user);
        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(appId, env, clusterName, gatewayNamespaceName, namespaceReleaseDTO);

        logger.info("set app : {} GatewayFlowRule success rules: {}", app, JSON.toJSONString(rules));
    }

    /**
     * 过滤不必要的字段
     *
     * @param rules
     */
    private void filterField(List<GatewayFlowRuleEntity> rules) {
        // 对不必要的信息进行过滤
        for (GatewayFlowRuleEntity rule : rules) {
            rule.setGmtCreate(null);
            rule.setGmtModified(null);
            rule.setIp(null);
            rule.setPort(null);
        }
    }

}
