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
package com.cloud.sentinel.dashboard.controller.gateway;


import com.cloud.sentinel.dashboard.auth.AuthAction;
import com.cloud.sentinel.dashboard.auth.AuthService;
import com.cloud.sentinel.dashboard.client.SentinelApiClient;
import com.cloud.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.gateway.GatewayParamFlowItemEntity;
import com.cloud.sentinel.dashboard.domain.Result;
import com.cloud.sentinel.dashboard.domain.vo.gateway.rule.AddFlowRuleReqVo;
import com.cloud.sentinel.dashboard.domain.vo.gateway.rule.GatewayParamFlowItemVo;
import com.cloud.sentinel.dashboard.domain.vo.gateway.rule.UpdateFlowRuleReqVo;
import com.cloud.sentinel.dashboard.repository.gateway.InMemGatewayFlowRuleStore;
import com.cloud.sentinel.dashboard.rule.DynamicRuleProvider;
import com.cloud.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.*;

/**
 * Gateway flow rule Controller for manage gateway flow rules.
 *
 * @author cdfive
 * @since 1.7.0
 */
@RestController
@RequestMapping(value = "/gateway/flow")
@Slf4j
public class GatewayFlowRuleController {

    @Autowired
    private InMemGatewayFlowRuleStore repository;

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Autowired
    @Qualifier("gatewayFlowRuleApolloProvider")
    private DynamicRuleProvider<List<GatewayFlowRuleEntity>> ruleProvider;

    @Autowired
    @Qualifier("gatewayFlowRuleApolloPublisher")
    private DynamicRulePublisher<List<GatewayFlowRuleEntity>> rulePublisher;

    /**
     * @description: 查询网关流控规则列表
     * @author: zhou shuai
     * @date: 2022/10/18 17:08
     * @param: app
     * @param: ip
     * @param: port
     * @return: com.alibaba.csp.sentinel.dashboard.domain.Result<java.util.List<com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity>>
     */
    @GetMapping("/list.json")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<GatewayFlowRuleEntity>> queryFlowRules(String app) {

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        try {
            List<GatewayFlowRuleEntity> rules = ruleProvider.getRules(app);
            repository.saveAll(rules);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            log.error("query gateway flow rules error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }


    /**
     * @description: 新增网关流控规则列表
     * @author: zhou shuai
     * @date: 2022/10/18 17:09
     * @param: reqVo
     * @return: com.alibaba.csp.sentinel.dashboard.domain.Result<com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity>
     */
    @PostMapping("/new.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<GatewayFlowRuleEntity> addFlowRule(@RequestBody AddFlowRuleReqVo reqVo) {

        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        GatewayFlowRuleEntity entity = new GatewayFlowRuleEntity();
        entity.setApp(app.trim());

        // API类型, Route ID或API分组
        Integer resourceMode = reqVo.getResourceMode();
        if (resourceMode == null) {
            return Result.ofFail(-1, "resourceMode can't be null");
        }
        if (!Arrays.asList(RESOURCE_MODE_ROUTE_ID, RESOURCE_MODE_CUSTOM_API_NAME).contains(resourceMode)) {
            return Result.ofFail(-1, "invalid resourceMode: " + resourceMode);
        }
        entity.setResourceMode(resourceMode);

        // API名称
        String resource = reqVo.getResource();
        if (StringUtil.isBlank(resource)) {
            return Result.ofFail(-1, "resource can't be null or empty");
        }
        entity.setResource(resource.trim());

        // 针对请求属性
        GatewayParamFlowItemVo paramItem = reqVo.getParamItem();
        if (paramItem != null) {
            GatewayParamFlowItemEntity itemEntity = new GatewayParamFlowItemEntity();
            entity.setParamItem(itemEntity);

            // 参数属性 0-ClientIP 1-Remote Host 2-Header 3-URL参数 4-Cookie
            Integer parseStrategy = paramItem.getParseStrategy();
            if (!Arrays.asList(PARAM_PARSE_STRATEGY_CLIENT_IP, PARAM_PARSE_STRATEGY_HOST, PARAM_PARSE_STRATEGY_HEADER
                    , PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                return Result.ofFail(-1, "invalid parseStrategy: " + parseStrategy);
            }
            itemEntity.setParseStrategy(paramItem.getParseStrategy());

            // 当参数属性为2-Header 3-URL参数 4-Cookie时，参数名称必填
            if (Arrays.asList(PARAM_PARSE_STRATEGY_HEADER, PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                // 参数名称
                String fieldName = paramItem.getFieldName();
                if (StringUtil.isBlank(fieldName)) {
                    return Result.ofFail(-1, "fieldName can't be null or empty");
                }
                itemEntity.setFieldName(paramItem.getFieldName());
            }

            String pattern = paramItem.getPattern();
            // 如果匹配串不为空，验证匹配模式
            if (StringUtil.isNotEmpty(pattern)) {
                itemEntity.setPattern(pattern);
                Integer matchStrategy = paramItem.getMatchStrategy();
                if (!Arrays.asList(PARAM_MATCH_STRATEGY_EXACT, PARAM_MATCH_STRATEGY_CONTAINS, PARAM_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                    return Result.ofFail(-1, "invalid matchStrategy: " + matchStrategy);
                }
                itemEntity.setMatchStrategy(matchStrategy);
            }
        }

        // 阈值类型 0-线程数 1-QPS
        Integer grade = reqVo.getGrade();
        if (grade == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (!Arrays.asList(FLOW_GRADE_THREAD, FLOW_GRADE_QPS).contains(grade)) {
            return Result.ofFail(-1, "invalid grade: " + grade);
        }
        entity.setGrade(grade);

        // QPS阈值
        Double count = reqVo.getCount();
        if (count == null) {
            return Result.ofFail(-1, "count can't be null");
        }
        if (count < 0) {
            return Result.ofFail(-1, "count should be at lease zero");
        }
        entity.setCount(count);

        // 间隔
        Long interval = reqVo.getInterval();
        if (interval == null) {
            return Result.ofFail(-1, "interval can't be null");
        }
        if (interval <= 0) {
            return Result.ofFail(-1, "interval should be greater than zero");
        }
        entity.setInterval(interval);

        // 间隔单位
        Integer intervalUnit = reqVo.getIntervalUnit();
        if (intervalUnit == null) {
            return Result.ofFail(-1, "intervalUnit can't be null");
        }
        if (!Arrays.asList(GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND, GatewayFlowRuleEntity.INTERVAL_UNIT_MINUTE, GatewayFlowRuleEntity.INTERVAL_UNIT_HOUR, GatewayFlowRuleEntity.INTERVAL_UNIT_DAY).contains(intervalUnit)) {
            return Result.ofFail(-1, "Invalid intervalUnit: " + intervalUnit);
        }
        entity.setIntervalUnit(intervalUnit);

        // 流控方式 0-快速失败 2-匀速排队
        Integer controlBehavior = reqVo.getControlBehavior();
        if (controlBehavior == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        if (!Arrays.asList(CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_RATE_LIMITER).contains(controlBehavior)) {
            return Result.ofFail(-1, "invalid controlBehavior: " + controlBehavior);
        }
        entity.setControlBehavior(controlBehavior);

        if (CONTROL_BEHAVIOR_DEFAULT == controlBehavior) {
            // 0-快速失败, 则Burst size必填
            Integer burst = reqVo.getBurst();
            if (burst == null) {
                return Result.ofFail(-1, "burst can't be null");
            }
            if (burst < 0) {
                return Result.ofFail(-1, "invalid burst: " + burst);
            }
            entity.setBurst(burst);
        } else if (CONTROL_BEHAVIOR_RATE_LIMITER == controlBehavior) {
            // 1-匀速排队, 则超时时间必填
            Integer maxQueueingTimeoutMs = reqVo.getMaxQueueingTimeoutMs();
            if (maxQueueingTimeoutMs == null) {
                return Result.ofFail(-1, "maxQueueingTimeoutMs can't be null");
            }
            if (maxQueueingTimeoutMs < 0) {
                return Result.ofFail(-1, "invalid maxQueueingTimeoutMs: " + maxQueueingTimeoutMs);
            }
            entity.setMaxQueueingTimeoutMs(maxQueueingTimeoutMs);
        }

        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);

        try {
            entity = repository.save(entity);
            publishRules(app);
        } catch (Throwable throwable) {
            log.error("add gateway flow rule error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    /**
     * @description: 更新网关流控规则列表
     * @author: zhou shuai
     * @date: 2022/10/18 17:09
     * @param: reqVo
     * @return: com.alibaba.csp.sentinel.dashboard.domain.Result<com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity>
     */
    @PostMapping("/save.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<GatewayFlowRuleEntity> updateFlowRule(@RequestBody UpdateFlowRuleReqVo reqVo) {

        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        Long id = reqVo.getId();
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        GatewayFlowRuleEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "gateway flow rule does not exist, id=" + id);
        }

        // 针对请求属性
        GatewayParamFlowItemVo paramItem = reqVo.getParamItem();
        if (paramItem != null) {
            GatewayParamFlowItemEntity itemEntity = new GatewayParamFlowItemEntity();
            entity.setParamItem(itemEntity);

            // 参数属性 0-ClientIP 1-Remote Host 2-Header 3-URL参数 4-Cookie
            Integer parseStrategy = paramItem.getParseStrategy();
            if (!Arrays.asList(PARAM_PARSE_STRATEGY_CLIENT_IP, PARAM_PARSE_STRATEGY_HOST, PARAM_PARSE_STRATEGY_HEADER
                    , PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                return Result.ofFail(-1, "invalid parseStrategy: " + parseStrategy);
            }
            itemEntity.setParseStrategy(paramItem.getParseStrategy());

            // 当参数属性为2-Header 3-URL参数 4-Cookie时，参数名称必填
            if (Arrays.asList(PARAM_PARSE_STRATEGY_HEADER, PARAM_PARSE_STRATEGY_URL_PARAM, PARAM_PARSE_STRATEGY_COOKIE).contains(parseStrategy)) {
                // 参数名称
                String fieldName = paramItem.getFieldName();
                if (StringUtil.isBlank(fieldName)) {
                    return Result.ofFail(-1, "fieldName can't be null or empty");
                }
                itemEntity.setFieldName(paramItem.getFieldName());
            }

            String pattern = paramItem.getPattern();
            // 如果匹配串不为空，验证匹配模式
            if (StringUtil.isNotEmpty(pattern)) {
                itemEntity.setPattern(pattern);
                Integer matchStrategy = paramItem.getMatchStrategy();
                if (!Arrays.asList(PARAM_MATCH_STRATEGY_EXACT, PARAM_MATCH_STRATEGY_CONTAINS, PARAM_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                    return Result.ofFail(-1, "invalid matchStrategy: " + matchStrategy);
                }
                itemEntity.setMatchStrategy(matchStrategy);
            }
        } else {
            entity.setParamItem(null);
        }

        // 阈值类型 0-线程数 1-QPS
        Integer grade = reqVo.getGrade();
        if (grade == null) {
            return Result.ofFail(-1, "grade can't be null");
        }
        if (!Arrays.asList(FLOW_GRADE_THREAD, FLOW_GRADE_QPS).contains(grade)) {
            return Result.ofFail(-1, "invalid grade: " + grade);
        }
        entity.setGrade(grade);

        // QPS阈值
        Double count = reqVo.getCount();
        if (count == null) {
            return Result.ofFail(-1, "count can't be null");
        }
        if (count < 0) {
            return Result.ofFail(-1, "count should be at lease zero");
        }
        entity.setCount(count);

        // 间隔
        Long interval = reqVo.getInterval();
        if (interval == null) {
            return Result.ofFail(-1, "interval can't be null");
        }
        if (interval <= 0) {
            return Result.ofFail(-1, "interval should be greater than zero");
        }
        entity.setInterval(interval);

        // 间隔单位
        Integer intervalUnit = reqVo.getIntervalUnit();
        if (intervalUnit == null) {
            return Result.ofFail(-1, "intervalUnit can't be null");
        }
        if (!Arrays.asList(GatewayFlowRuleEntity.INTERVAL_UNIT_SECOND, GatewayFlowRuleEntity.INTERVAL_UNIT_MINUTE, GatewayFlowRuleEntity.INTERVAL_UNIT_HOUR, GatewayFlowRuleEntity.INTERVAL_UNIT_DAY).contains(intervalUnit)) {
            return Result.ofFail(-1, "Invalid intervalUnit: " + intervalUnit);
        }
        entity.setIntervalUnit(intervalUnit);

        // 流控方式 0-快速失败 2-匀速排队
        Integer controlBehavior = reqVo.getControlBehavior();
        if (controlBehavior == null) {
            return Result.ofFail(-1, "controlBehavior can't be null");
        }
        if (!Arrays.asList(CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_RATE_LIMITER).contains(controlBehavior)) {
            return Result.ofFail(-1, "invalid controlBehavior: " + controlBehavior);
        }
        entity.setControlBehavior(controlBehavior);

        if (CONTROL_BEHAVIOR_DEFAULT == controlBehavior) {
            // 0-快速失败, 则Burst size必填
            Integer burst = reqVo.getBurst();
            if (burst == null) {
                return Result.ofFail(-1, "burst can't be null");
            }
            if (burst < 0) {
                return Result.ofFail(-1, "invalid burst: " + burst);
            }
            entity.setBurst(burst);
        } else if (CONTROL_BEHAVIOR_RATE_LIMITER == controlBehavior) {
            // 2-匀速排队, 则超时时间必填
            Integer maxQueueingTimeoutMs = reqVo.getMaxQueueingTimeoutMs();
            if (maxQueueingTimeoutMs == null) {
                return Result.ofFail(-1, "maxQueueingTimeoutMs can't be null");
            }
            if (maxQueueingTimeoutMs < 0) {
                return Result.ofFail(-1, "invalid maxQueueingTimeoutMs: " + maxQueueingTimeoutMs);
            }
            entity.setMaxQueueingTimeoutMs(maxQueueingTimeoutMs);
        }

        Date date = new Date();
        entity.setGmtModified(date);

        try {
            entity = repository.save(entity);
            publishRules(app);
        } catch (Throwable throwable) {
            log.error("update gateway flow rule error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }


    /**
     * @description: 删除网关流控规则列表
     * @author: zhou shuai
     * @date: 2022/10/18 17:10
     * @param: id
     * @return: com.alibaba.csp.sentinel.dashboard.domain.Result<java.lang.Long>
     */
    @PostMapping("/delete.json")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)
    public Result<Long> deleteFlowRule(Long id) {

        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        GatewayFlowRuleEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }

        try {
            repository.delete(id);
            publishRules(oldEntity.getApp());
        } catch (Throwable throwable) {
            log.error("delete gateway flow rule error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(id);
    }


    /**
     * @description: 发布网关流控规则列表
     * @author: zhou shuai
     * @date: 2022/10/18 17:10
     * @param: app
     * @param: ip
     * @param: port
     * @return: boolean
     */
    private void publishRules(String app) {
        List<GatewayFlowRuleEntity> rules = repository.findAllByApp(app);
        try {
            rulePublisher.publish(app, rules);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
