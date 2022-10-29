package com.cloud.sentinel.dashboard.datasource.entity.rule.correct;

import com.cloud.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.common.CommonEntity;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import lombok.Data;

import java.util.*;

/**
 * @author: zhou shuai
 * @description: 重写 ParamFlowRuleEntity
 * @date: 2022/10/22 10:04
 */
@Data
public class ParamFlowRuleCorrectEntity extends CommonEntity implements RuleEntity {

    /********************ParamFlowRule属性*********************************/
    private int grade = 1;
    private Integer paramIdx;
    private double count;
    private int controlBehavior = 0;
    private int maxQueueingTimeMs = 0;
    private int burstCount = 0;
    private long durationInSec = 1L;
    private List<ParamFlowItem> paramFlowItemList = new ArrayList();
    private Map<Object, Integer> hotItems = new HashMap();
    private boolean clusterMode = false;
    private ParamFlowClusterConfig clusterConfig;


    @Override
    public Rule toRule(){
        ParamFlowRule rule=new ParamFlowRule();
        return rule;
    }

}
