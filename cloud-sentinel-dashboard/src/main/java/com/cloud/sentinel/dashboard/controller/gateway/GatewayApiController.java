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
import com.cloud.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.cloud.sentinel.dashboard.datasource.entity.gateway.ApiPredicateItemEntity;
import com.cloud.sentinel.dashboard.domain.Result;
import com.cloud.sentinel.dashboard.domain.vo.gateway.api.AddApiReqVo;
import com.cloud.sentinel.dashboard.domain.vo.gateway.api.ApiPredicateItemVo;
import com.cloud.sentinel.dashboard.domain.vo.gateway.api.UpdateApiReqVo;
import com.cloud.sentinel.dashboard.repository.gateway.InMemApiDefinitionStore;
import com.cloud.sentinel.dashboard.rule.DynamicRuleProvider;
import com.cloud.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;

/**
 * Gateway api Controller for manage gateway api definitions.
 *
 * @author cdfive
 * @since 1.7.0
 */
@RestController
@RequestMapping(value = "/gateway/api")
@Slf4j
public class GatewayApiController {

    @Autowired
    private InMemApiDefinitionStore repository;

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Autowired
    @Qualifier("gatewayApiRuleApolloProvider")
    private DynamicRuleProvider<List<ApiDefinitionEntity>> ruleProvider;

    @Autowired
    @Qualifier("gatewayApiRuleApolloPublisher")
    private DynamicRulePublisher<List<ApiDefinitionEntity>> rulePublisher;

    @GetMapping("/list.json")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<ApiDefinitionEntity>> queryApis(String app, String ip, Integer port) {

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }
        if (StringUtil.isEmpty(ip)) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }

        try {
            List<ApiDefinitionEntity> apis = ruleProvider.getRules(app);
            repository.saveAll(apis);
            return Result.ofSuccess(apis);
        } catch (Throwable throwable) {
            log.error("queryApis error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }


    @PostMapping("/new.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<ApiDefinitionEntity> addApi(HttpServletRequest request, @RequestBody AddApiReqVo reqVo) {

        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        ApiDefinitionEntity entity = new ApiDefinitionEntity();
        entity.setApp(app.trim());

        // API名称
        String apiName = reqVo.getApiName();
        if (StringUtil.isBlank(apiName)) {
            return Result.ofFail(-1, "apiName can't be null or empty");
        }
        entity.setApiName(apiName.trim());

        // 匹配规则列表
        List<ApiPredicateItemVo> predicateItems = reqVo.getPredicateItems();
        if (CollectionUtils.isEmpty(predicateItems)) {
            return Result.ofFail(-1, "predicateItems can't empty");
        }

        List<ApiPredicateItemEntity> predicateItemEntities = new ArrayList<>();
        for (ApiPredicateItemVo predicateItem : predicateItems) {
            ApiPredicateItemEntity predicateItemEntity = new ApiPredicateItemEntity();

            // 匹配模式
            Integer matchStrategy = predicateItem.getMatchStrategy();
            if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                return Result.ofFail(-1, "invalid matchStrategy: " + matchStrategy);
            }
            predicateItemEntity.setMatchStrategy(matchStrategy);

            // 匹配串
            String pattern = predicateItem.getPattern();
            if (StringUtil.isBlank(pattern)) {
                return Result.ofFail(-1, "pattern can't be null or empty");
            }
            predicateItemEntity.setPattern(pattern);

            predicateItemEntities.add(predicateItemEntity);
        }
        entity.setPredicateItems(new LinkedHashSet<>(predicateItemEntities));

        // 检查API名称不能重复
        List<ApiDefinitionEntity> allApis = repository.findAllByApp(app);
        if (allApis.stream().map(o -> o.getApiName()).anyMatch(o -> o.equals(apiName.trim()))) {
            return Result.ofFail(-1, "apiName exists: " + apiName);
        }

        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);

        try {
            entity = repository.save(entity);
            publishApis(app);
        } catch (Throwable throwable) {
            log.error("add gateway api error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PostMapping("/save.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<ApiDefinitionEntity> updateApi(@RequestBody UpdateApiReqVo reqVo) {
        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        Long id = reqVo.getId();
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        ApiDefinitionEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "api does not exist, id=" + id);
        }

        // 匹配规则列表
        List<ApiPredicateItemVo> predicateItems = reqVo.getPredicateItems();
        if (CollectionUtils.isEmpty(predicateItems)) {
            return Result.ofFail(-1, "predicateItems can't empty");
        }

        List<ApiPredicateItemEntity> predicateItemEntities = new ArrayList<>();
        for (ApiPredicateItemVo predicateItem : predicateItems) {
            ApiPredicateItemEntity predicateItemEntity = new ApiPredicateItemEntity();

            // 匹配模式
            int matchStrategy = predicateItem.getMatchStrategy();
            if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                return Result.ofFail(-1, "Invalid matchStrategy: " + matchStrategy);
            }
            predicateItemEntity.setMatchStrategy(matchStrategy);

            // 匹配串
            String pattern = predicateItem.getPattern();
            if (StringUtil.isBlank(pattern)) {
                return Result.ofFail(-1, "pattern can't be null or empty");
            }
            predicateItemEntity.setPattern(pattern);

            predicateItemEntities.add(predicateItemEntity);
        }
        entity.setPredicateItems(new LinkedHashSet<>(predicateItemEntities));

        Date date = new Date();
        entity.setGmtModified(date);

        try {
            entity = repository.save(entity);
            publishApis(app);
        } catch (Throwable throwable) {
            log.error("update gateway api error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(entity);
    }

    @PostMapping("/delete.json")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)

    public Result<Long> deleteApi(Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        ApiDefinitionEntity oldEntity = repository.findById(id);
        if (oldEntity == null) {
            return Result.ofSuccess(null);
        }

        try {
            repository.delete(id);
            publishApis(oldEntity.getApp());
        } catch (Throwable throwable) {
            log.error("delete gateway api error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
        return Result.ofSuccess(id);
    }


    private void publishApis(String app) {
        List<ApiDefinitionEntity> rules = repository.findAllByApp(app);
        try {
            rulePublisher.publish(app, rules);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
