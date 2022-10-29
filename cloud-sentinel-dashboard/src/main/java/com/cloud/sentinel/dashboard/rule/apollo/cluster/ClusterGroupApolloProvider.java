package com.cloud.sentinel.dashboard.rule.apollo.cluster;

import com.cloud.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
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
 * @description: 集群流控
 * @author: 01398395
 * @create: 2020-07-22 14:17
 **/
@Component("clusterGroupApolloProvider")
public class ClusterGroupApolloProvider implements DynamicRuleProvider<List<ClusterAppAssignMap>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<String, List<ClusterAppAssignMap>> converter;
    @Value("${app.id}")
    private String appId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${apollo.clusterName}")
    private String clusterName;
    @Value("${apollo.namespaceName}")
    private String namespaceName;

    @Override
    public List<ClusterAppAssignMap> getRules(String appName){
        String flowDataId = ApolloConfigUtil.getClusterGroupDataId(appName);
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, env, clusterName, namespaceName);
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
