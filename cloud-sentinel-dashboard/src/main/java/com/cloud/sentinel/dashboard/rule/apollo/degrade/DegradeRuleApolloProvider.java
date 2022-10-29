package com.cloud.sentinel.dashboard.rule.apollo.degrade;

import com.cloud.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.cloud.sentinel.dashboard.rule.DynamicRuleProvider;
import com.cloud.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: sentinel-parent
 * @description: 降级规则
 * @author: 01398395
 * @create: 2020-07-21 16:36
 **/
@Component("degradeRuleApolloProvider")
public class DegradeRuleApolloProvider implements DynamicRuleProvider<List<DegradeRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<String, List<DegradeRuleEntity>> converter;

    @Value("${app.id}")
    private String appId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${apollo.clusterName}")
    private String clusterName;
    @Value("${apollo.namespaceName}")
    private String namespaceName;
    @Value("${apollo.gateway.namespaceName}")
    private String gatewayNamespaceName;

    @Override
    public List<DegradeRuleEntity> getRules(String appName) {
        String flowDataId = ApolloConfigUtil.getDegradeDataId(appName);
        OpenNamespaceDTO openNamespaceDTO;
        if (ApolloConfigUtil.isGatewayAppName(appName)) {
            openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, env, clusterName, gatewayNamespaceName);
        } else {
            openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, env, clusterName, namespaceName);
        }
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(flowDataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }
}
