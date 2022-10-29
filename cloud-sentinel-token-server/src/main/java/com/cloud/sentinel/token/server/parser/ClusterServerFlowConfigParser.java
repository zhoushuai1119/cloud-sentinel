package com.cloud.sentinel.token.server.parser;

import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.cloud.sentinel.token.server.entity.ClusterGroupEntity;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

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
            ClusterGroupEntity clusterGroup = groupList.get(0);
            return new ServerFlowConfig()
                    .setExceedCount(ClusterServerConfigManager.getExceedCount())
                    .setIntervalMs(ClusterServerConfigManager.getIntervalMs())
                    .setMaxAllowedQps(clusterGroup.getMaxAllowedQps())
                    .setMaxOccupyRatio(ClusterServerConfigManager.getMaxOccupyRatio())
                    .setSampleCount(ClusterServerConfigManager.getSampleCount());
        }
        return null;
    }

}

