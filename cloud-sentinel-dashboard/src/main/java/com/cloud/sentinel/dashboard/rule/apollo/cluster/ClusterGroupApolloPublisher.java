package com.cloud.sentinel.dashboard.rule.apollo.cluster;

import com.cloud.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.cloud.sentinel.dashboard.rule.DynamicRulePublisher;
import com.cloud.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: sentinel-parent
 * @description: 集群流控
 * @author: 01398395
 * @create: 2020-07-22 14:23
 **/
@Component("clusterGroupApolloPublisher")
public class ClusterGroupApolloPublisher implements DynamicRulePublisher<List<ClusterAppAssignMap>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<List<ClusterAppAssignMap>, String> converter;
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

    @Override
    public void publish(String app, List<ClusterAppAssignMap> rules){
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        // Increase the configuration
        String flowDataId = ApolloConfigUtil.getClusterGroupDataId(app);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(converter.convert(rules));
        openItemDTO.setComment(app + "集群流控");
        openItemDTO.setDataChangeCreatedBy(user);
        apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespaceName, openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        namespaceReleaseDTO.setReleasedBy(user);
        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(appId, env, clusterName, namespaceName, namespaceReleaseDTO);
    }

}
