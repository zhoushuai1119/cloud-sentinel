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

import com.cloud.sentinel.dashboard.datasource.entity.rule.common.CommonEntity;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import lombok.Data;

/**
 * @author leyou
 */
@Data
public class FlowRuleEntity extends CommonEntity implements RuleEntity {

    /**
     * 0为线程数;1为qps
     */
    private Integer grade;
    private Double count;
    /**
     * 0为直接限流;1为关联限流;2为链路限流
     ***/
    private Integer strategy;
    private String refResource;
    /**
     * 0. default, 1. warm up, 2. rate limiter
     */
    private Integer controlBehavior;
    private Integer warmUpPeriodSec;
    /**
     * max queueing time in rate limiter behavior
     */
    private Integer maxQueueingTimeMs;
    /**
     * 是否为集群模式
     */
    private boolean clusterMode;
    /**
     * Flow rule config for cluster mode.
     */
    private ClusterFlowConfig clusterConfig;

    public static FlowRuleEntity fromFlowRule(String app, String ip, Integer port, FlowRule rule) {
        FlowRuleEntity entity = new FlowRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setLimitApp(rule.getLimitApp());
        entity.setResource(rule.getResource());
        entity.setGrade(rule.getGrade());
        entity.setCount(rule.getCount());
        entity.setStrategy(rule.getStrategy());
        entity.setRefResource(rule.getRefResource());
        entity.setControlBehavior(rule.getControlBehavior());
        entity.setWarmUpPeriodSec(rule.getWarmUpPeriodSec());
        entity.setMaxQueueingTimeMs(rule.getMaxQueueingTimeMs());
        entity.setClusterMode(rule.isClusterMode());
        entity.setClusterConfig(rule.getClusterConfig());
        return entity;
    }


    @Override
    public FlowRule toRule() {
        FlowRule flowRule = new FlowRule();
        flowRule.setCount(this.count);
        flowRule.setGrade(this.grade);
        flowRule.setResource(super.getResource());
        flowRule.setLimitApp(super.getLimitApp());
        flowRule.setRefResource(this.refResource);
        flowRule.setStrategy(this.strategy);
        if (this.controlBehavior != null) {
            flowRule.setControlBehavior(controlBehavior);
        }
        if (this.warmUpPeriodSec != null) {
            flowRule.setWarmUpPeriodSec(warmUpPeriodSec);
        }
        if (this.maxQueueingTimeMs != null) {
            flowRule.setMaxQueueingTimeMs(maxQueueingTimeMs);
        }
        flowRule.setClusterMode(clusterMode);
        flowRule.setClusterConfig(clusterConfig);
        return flowRule;
    }

}
