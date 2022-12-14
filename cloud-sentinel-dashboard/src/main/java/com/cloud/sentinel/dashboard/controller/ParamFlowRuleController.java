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
package com.cloud.sentinel.dashboard.controller;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.cloud.sentinel.dashboard.auth.AuthAction;
import com.cloud.sentinel.dashboard.auth.AuthService;
import com.cloud.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.cloud.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.cloud.sentinel.dashboard.domain.Result;
import com.cloud.sentinel.dashboard.repository.rule.RuleRepository;
import com.cloud.sentinel.dashboard.rule.DynamicRuleProvider;
import com.cloud.sentinel.dashboard.rule.DynamicRulePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
@RestController
@RequestMapping(value = "/paramFlow")
@Slf4j
public class ParamFlowRuleController {

    @Autowired
    private RuleRepository<ParamFlowRuleEntity, Long> repository;
    @Autowired
    @Qualifier("paramFlowRuleApolloProvider")
    private DynamicRuleProvider<List<ParamFlowRuleEntity>> ruleProvider;
    @Autowired
    @Qualifier("paramFlowRuleApolloPublisher")
    private DynamicRulePublisher<List<ParamFlowRuleEntity>> rulePublisher;


    @GetMapping("/rules")
    @AuthAction(PrivilegeType.READ_RULE)
    public Result<List<ParamFlowRuleEntity>> apiQueryAllRulesForMachine(@RequestParam String app) {
        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app cannot be null or empty");
        }
        try {
            List<ParamFlowRuleEntity> rules = ruleProvider.getRules(app);
            return Result.ofSuccess(rules);
        } catch (ExecutionException ex) {
            log.error("Error when querying parameter flow rules", ex.getCause());
            return Result.ofThrowable(-1, ex.getCause());
        } catch (Throwable throwable) {
            log.error("Error when querying parameter flow rules", throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }


    @PostMapping("/rule")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<ParamFlowRuleEntity> apiAddParamFlowRule(@RequestBody ParamFlowRuleEntity entity) {
        Result<ParamFlowRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(null);
        entity.getRule().setResource(entity.getResource().trim());
        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            publishRules(entity.getApp());
            return Result.ofSuccess(entity);
        } catch (Exception ex) {
            log.error("Error when adding new parameter flow rules", ex.getCause());
            return Result.ofThrowable(-1, ex.getCause());
        } catch (Throwable throwable) {
            log.error("Error when adding new parameter flow rules", throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    private <R> Result<R> checkEntityInternal(ParamFlowRuleEntity entity) {
        if (entity == null) {
            return Result.ofFail(-1, "bad rule body");
        }
        if (StringUtil.isBlank(entity.getApp())) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (entity.getRule() == null) {
            return Result.ofFail(-1, "rule can't be null");
        }
        if (StringUtil.isBlank(entity.getResource())) {
            return Result.ofFail(-1, "resource name cannot be null or empty");
        }
        if (entity.getCount() < 0) {
            return Result.ofFail(-1, "count should be valid");
        }
        if (entity.getGrade() != RuleConstant.FLOW_GRADE_QPS) {
            return Result.ofFail(-1, "Unknown mode (blockGrade) for parameter flow control");
        }
        if (entity.getParamIdx() == null || entity.getParamIdx() < 0) {
            return Result.ofFail(-1, "paramIdx should be valid");
        }
        if (entity.getDurationInSec() <= 0) {
            return Result.ofFail(-1, "durationInSec should be valid");
        }
        if (entity.getControlBehavior() < 0) {
            return Result.ofFail(-1, "controlBehavior should be valid");
        }
        return null;
    }

    @PutMapping("/rule/{id}")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<ParamFlowRuleEntity> apiUpdateParamFlowRule(@PathVariable("id") Long id,
                                                              @RequestBody ParamFlowRuleEntity entity) {
        if (id == null || id <= 0) {
            return Result.ofFail(-1, "Invalid id");
        }
        ParamFlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofFail(-1, "id " + id + " does not exist");
        }

        Result<ParamFlowRuleEntity> checkResult = checkEntityInternal(entity);
        if (checkResult != null) {
            return checkResult;
        }
        entity.setId(id);
        Date date = new Date();
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(date);
        try {
            entity = repository.save(entity);
            publishRules(entity.getApp());
            return Result.ofSuccess(entity);
        } catch (Exception ex) {
            log.error("Error when updating parameter flow rules, id=" + id, ex.getCause());
            return Result.ofThrowable(-1, ex.getCause());
        } catch (Throwable throwable) {
            log.error("Error when updating parameter flow rules, id=" + id, throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    @DeleteMapping("/rule/{id}")
    @AuthAction(PrivilegeType.DELETE_RULE)
    public Result<Long> apiDeleteRule(@PathVariable("id") Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id cannot be null");
        }
        ParamFlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }

        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
            return Result.ofSuccess(id);
        } catch (Exception ex) {
            log.error("Error when deleting parameter flow rules", ex.getCause());
            return Result.ofThrowable(-1, ex.getCause());
        } catch (Throwable throwable) {
            log.error("Error when deleting parameter flow rules", throwable);
            return Result.ofFail(-1, throwable.getMessage());
        }
    }

    private void publishRules(String app) {
        List<ParamFlowRuleEntity> rules = repository.findAllByApp(app);
        try {
            rulePublisher.publish(app, rules);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
