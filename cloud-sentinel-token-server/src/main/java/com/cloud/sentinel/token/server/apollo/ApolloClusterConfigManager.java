package com.cloud.sentinel.token.server.apollo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cloud.dingtalk.dinger.DingerSender;
import com.cloud.sentinel.token.server.entity.ClusterGroupEntity;
import com.cloud.sentinel.token.server.utils.ApolloConfigUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @description:
 * @author: zhou shuai
 * @date: 2022/11/21 20:01
 * @version: v1
 */
@Component
@Slf4j
public class ApolloClusterConfigManager {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Autowired
    private DingerSender dingerSender;

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

    /**
     * 成为master的tokenServer修改不同规则namespace中的集群配置
     *
     * @param ip
     * @param port
     */
    public void changeMasterTokenServerAddress(String ip, Integer port) {
        //查询token server nameSpace
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, env, clusterName, namespaceName);
        List<OpenItemDTO> itemDTOList = openNamespaceDTO.getItems();
        if (CollectionUtils.isEmpty(itemDTOList)) {
            return;
        }
        //找到配置了集群限流的item
        Optional<OpenItemDTO> clusterConfigItem =
                itemDTOList.stream().filter(t -> ApolloConfigUtil.getTokenServerRuleKey().equals(t.getKey())).findFirst();
        if (!clusterConfigItem.isPresent()) {
            return;
        }
        publishMasterTokenServerAddress(clusterConfigItem.get(), ip, port);
    }

    /**
     * 将master TokenServer的配置写入不同规则namespace的集群配置中
     *
     * @param openItemDTO 集群规则所在的item
     * @param ip          tokenServer ip
     * @param port        tokenServer port
     */
    private void publishMasterTokenServerAddress(OpenItemDTO openItemDTO, String ip, Integer port) {
        String value = openItemDTO.getValue();
        if (StringUtils.isEmpty(value)) {
            return;
        }
        try {
            List<ClusterGroupEntity> groupList = JSON.parseObject(value, new TypeReference<List<ClusterGroupEntity>>() {

            });

            if (CollectionUtils.isEmpty(groupList)) {
                return;
            }

            ClusterGroupEntity clusterGroupEntity = groupList.get(0);

            //规则中的tokenServer地址与当前相等，不做处理
            if (Objects.equals(clusterGroupEntity.getIp(), ip) && Objects.equals(clusterGroupEntity.getPort(), port)) {
                return;
            }

            clusterGroupEntity.setIp(ip);
            clusterGroupEntity.setPort(port);

            openItemDTO.setValue(JSON.toJSONString(groupList));
            apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespaceName, openItemDTO);

            // Release configuration
            NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
            namespaceReleaseDTO.setEmergencyPublish(true);
            namespaceReleaseDTO.setReleaseComment("Modify Token Server Config");
            namespaceReleaseDTO.setReleasedBy(user);
            namespaceReleaseDTO.setReleaseTitle("Modify Token Server Config");
            apolloOpenApiClient.publishNamespace(appId, env, clusterName, namespaceName, namespaceReleaseDTO);

            log.info("Token Server 地址修改成功，namespaceName:【" + namespaceName + "】");
            //发送企业微信告警
            dingerSender.send("Token Server 地址修改成功，namespaceName:【" + namespaceName + "】");
        } catch (Exception e) {
            log.error("Token Server 地址修改失败，namespaceName:【" + namespaceName + "】");
            //发送企业微信告警
            dingerSender.send("Token Server 地址修改失败，namespaceName:【" + namespaceName + "】");
        }

    }

}
