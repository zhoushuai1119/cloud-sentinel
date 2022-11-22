package com.cloud.sentinel.token.server.parser;

import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
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
 * @date: 2022/10/25 19:51
 * @version: v1
 */
public class TokenServerTransportConfigParser implements Converter<String, ServerTransportConfig> {

    @Override
    public ServerTransportConfig convert(String source) {
        if (source == null) {
            return null;
        }
        RecordLog.info("[TokenServerTransportConfigParser] Get data: " + source);
        List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {
        });
        if (CollectionUtils.isEmpty(groupList)) {
            return null;
        }
        return extractServerTransportConfig(groupList);
    }

    private ServerTransportConfig extractServerTransportConfig(List<ClusterGroupEntity> groupList) {
        if (CollectionUtils.isNotEmpty(groupList)) {
            for (ClusterGroupEntity group : groupList) {
                if (Objects.equals(group.getIp(), HostNameUtil.getIp())) {
                    return new ServerTransportConfig().setPort(group.getPort()).setIdleSeconds(600);
                }
            }
        }
        return null;
    }

}


