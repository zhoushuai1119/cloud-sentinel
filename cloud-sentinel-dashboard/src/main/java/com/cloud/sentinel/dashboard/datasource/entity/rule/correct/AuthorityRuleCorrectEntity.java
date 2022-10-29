package com.cloud.sentinel.dashboard.datasource.entity.rule.correct;

import com.cloud.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.common.CommonEntity;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import lombok.Data;

/**
 * @author: zhou shuai
 * @description: 重写 AuthorityRuleEntity
 * @date: 2022/10/22 10:04
 */
@Data
public class AuthorityRuleCorrectEntity extends CommonEntity implements RuleEntity {


    /********************AuthorityRule属性*********************************/
    /**
     * 0-白名单; 1-黑名单
     */
    private int strategy = 0;


    @Override
    public Rule toRule(){
        AuthorityRule rule=new AuthorityRule();
        return rule;
    }

}
