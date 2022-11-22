package com.cloud.sentinel.token.server.parser;

import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cloud.sentinel.token.server.entity.ClusterGroupEntity;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: zhou shuai
 * @date: 2022/10/25 19:44
 * @version: v1
 */
public class ClusterServerFlowConfigParser implements Converter<String, ServerFlowConfig> {
    @Override
    public ServerFlowConfig convert(String source) {
        if (source == null) {
            return null;
        }
        RecordLog.info("[ClusterServerFlowConfigParser] Get data: " + source);
        List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {
        });
        if (groupList == null || groupList.isEmpty()) {
            return null;
        }
        return extractServerFlowConfig(groupList);
    }

    private ServerFlowConfig extractServerFlowConfig(List<ClusterGroupEntity> groupList) {
        if (CollectionUtils.isNotEmpty(groupList)) {
            for (ClusterGroupEntity group : groupList) {
                if (Objects.equals(group.getIp(), HostNameUtil.getIp())) {
                    return new ServerFlowConfig()
                            .setExceedCount(ClusterServerConfigManager.getExceedCount())
                            .setIntervalMs(ClusterServerConfigManager.getIntervalMs())
                            .setMaxAllowedQps(group.getMaxAllowedQps())
                            .setMaxOccupyRatio(ClusterServerConfigManager.getMaxOccupyRatio())
                            .setSampleCount(ClusterServerConfigManager.getSampleCount());
                }
            }
        }
        return null;
    }

}

