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
package com.cloud.sentinel.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import lombok.Data;

import java.util.Date;

/**
 * @author leyou
 */
@Data
public class SystemRuleEntity implements RuleEntity {

    private Long id;
    private String app;
    private String ip;
    private Integer port;
    private Date gmtCreate;
    private Date gmtModified;

    private Double highestSystemLoad;
    private Long avgRt;
    private Long maxThread;
    private Double qps;
    private Double highestCpuUsage;


    public static SystemRuleEntity fromSystemRule(String app, String ip, Integer port, SystemRule rule) {
        SystemRuleEntity entity = new SystemRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setHighestSystemLoad(rule.getHighestSystemLoad());
        entity.setHighestCpuUsage(rule.getHighestCpuUsage());
        entity.setAvgRt(rule.getAvgRt());
        entity.setMaxThread(rule.getMaxThread());
        entity.setQps(rule.getQps());
        return entity;
    }


    @Override
    public SystemRule toRule() {
        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(highestSystemLoad);
        rule.setAvgRt(avgRt);
        rule.setMaxThread(maxThread);
        rule.setQps(qps);
        rule.setHighestCpuUsage(highestCpuUsage);
        return rule;
    }

}
