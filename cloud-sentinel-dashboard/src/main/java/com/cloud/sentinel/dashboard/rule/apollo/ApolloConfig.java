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
package com.cloud.sentinel.dashboard.rule.apollo;

import com.cloud.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.cloud.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.correct.AuthorityRuleCorrectEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.correct.ParamFlowRuleCorrectEntity;
import com.cloud.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.cloud.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author 01398395
 * @since 1.5.0
 */
@Configuration
public class ApolloConfig {

    @Value("${apollo.meta}")
    private String portalUrl;
    @Value("${apollo.token}")
    private String token;

    /**
     * 流控规则编码
     *
     * @return
     */
    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 流控规则解码
     *
     * @return
     */
    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    /**
     * 降级规则编码
     *
     * @return
     */
    @Bean
    public Converter<List<DegradeRuleEntity>, String> degradeRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 降级规则解码
     *
     * @return
     */
    @Bean
    public Converter<String, List<DegradeRuleEntity>> degradeRuleEntityDecoder() {
        return s -> JSON.parseArray(s, DegradeRuleEntity.class);
    }


    /**
     * 授权规则改造编码
     *
     * @return
     */
    @Bean
    public Converter<List<AuthorityRuleCorrectEntity>, String> authorityRuleCorrectEntityEncoder() {
        return JSON::toJSONString;
    }


    /**
     * 授权规则改造解码
     *
     * @return
     */
    @Bean
    public Converter<String, List<AuthorityRuleCorrectEntity>> authorityRuleCorrectEntityDecoder() {
        return s -> JSON.parseArray(s, AuthorityRuleCorrectEntity.class);
    }

    /**
     * 系统规则编码
     *
     * @return
     */
    @Bean
    public Converter<List<SystemRuleEntity>, String> systemRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 系统规则解码
     *
     * @return
     */
    @Bean
    public Converter<String, List<SystemRuleEntity>> systemRuleEntityDecoder() {
        return s -> JSON.parseArray(s, SystemRuleEntity.class);
    }


    /**
     * 热点规则改造编码
     *
     * @return
     */
    @Bean
    public Converter<List<ParamFlowRuleCorrectEntity>, String> paramFlowRuleCorrectEntityEncoder() {
        return JSON::toJSONString;
    }


    /**
     * 热点规则改造解码
     *
     * @return
     */
    @Bean
    public Converter<String, List<ParamFlowRuleCorrectEntity>> paramFlowRuleCorrectEntityDecoder() {
        return s -> JSON.parseArray(s, ParamFlowRuleCorrectEntity.class);
    }


    /**
     * 集群流控规则编码
     *
     * @return
     */
    @Bean
    public Converter<List<ClusterAppAssignMap>, String> clusterGroupEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 集群流控规则解码
     *
     * @return
     */
    @Bean
    public Converter<String, List<ClusterAppAssignMap>> clusterGroupEntityDecoder() {
        return s -> JSON.parseArray(s, ClusterAppAssignMap.class);
    }

    /**
     * API管理分组编码
     */
    @Bean
    public Converter<List<ApiDefinitionEntity>, String> apiDefinitionEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * API管理分组解码
     */
    @Bean
    public Converter<String, List<ApiDefinitionEntity>> apiDefinitionEntityDecoder() {
        return s -> JSON.parseArray(s, ApiDefinitionEntity.class);
    }

    /**
     * 网关流控规则编码
     */
    @Bean
    public Converter<List<GatewayFlowRuleEntity>, String> gatewayFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 网关流控规则解码
     */
    @Bean
    public Converter<String, List<GatewayFlowRuleEntity>> gatewayFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, GatewayFlowRuleEntity.class);
    }

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder()
            .withPortalUrl(portalUrl)
            .withToken(token)
            .build();
        return client;
    }

}
