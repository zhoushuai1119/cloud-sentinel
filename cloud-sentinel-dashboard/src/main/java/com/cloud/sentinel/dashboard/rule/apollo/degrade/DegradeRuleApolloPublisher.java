package com.cloud.sentinel.dashboard.rule.apollo.degrade;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.cloud.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.cloud.sentinel.dashboard.rule.DynamicRulePublisher;
import com.cloud.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: sentinel-parent
 * @description:
 * @author: 01398395
 * @create: 2020-07-21 16:40
 **/
@Component("degradeRuleApolloPublisher")
@Slf4j
public class DegradeRuleApolloPublisher implements DynamicRulePublisher<List<DegradeRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<List<DegradeRuleEntity>, String> converter;
    @Value("${app.id}")
    private String appId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${apollo.user}")
    private String user;
    @Value("${apollo.clusterName}")
    private String clusterName;
    @Value("${apollo.namespaceName}")
    private String namespaceName;
    @Value("${apollo.gateway.namespaceName}")
    private String gatewayNamespaceName;

    @Override
    public void publish(String app, List<DegradeRuleEntity> rules) {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        filterField(rules);
        String spaceName = namespaceName;
        if (ApolloConfigUtil.isGatewayAppName(app)) {
            spaceName = gatewayNamespaceName;
        }
        // Increase the configuration
        String flowDataId = ApolloConfigUtil.getDegradeDataId(app);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(converter.convert(rules));
        openItemDTO.setComment(app + "降级规则");
        openItemDTO.setDataChangeCreatedBy(user);
        apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, spaceName, openItemDTO);

        // 发布配置
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        namespaceReleaseDTO.setReleasedBy(user);
        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(appId, env, clusterName, spaceName, namespaceReleaseDTO);

        log.info("set app : {} DegradeRule success rules: {}", app, JSON.toJSONString(rules));
    }

    /**
     * 过滤不必要的字段
     *
     * @param rules
     */
    private void filterField(List<DegradeRuleEntity> rules) {
        // 对不必要的信息进行过滤
        for (DegradeRuleEntity rule : rules) {
            rule.setGmtCreate(null);
            rule.setGmtModified(null);
            rule.setIp(null);
            rule.setPort(null);
        }
    }

}
