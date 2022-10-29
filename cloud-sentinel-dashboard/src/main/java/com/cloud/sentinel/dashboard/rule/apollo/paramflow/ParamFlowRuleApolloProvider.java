package com.cloud.sentinel.dashboard.rule.apollo.paramflow;

import com.cloud.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.correct.ParamFlowRuleCorrectEntity;
import com.cloud.sentinel.dashboard.rule.DynamicRuleProvider;
import com.cloud.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: sentinel-parent
 * @description: 热点
 * @author: 01398395
 * @create: 2020-07-22 10:40
 **/
@Component("paramFlowRuleApolloProvider")
public class ParamFlowRuleApolloProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<String, List<ParamFlowRuleCorrectEntity>> converter;
    @Value("${app.id}")
    private String appId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${apollo.clusterName}")
    private String clusterName;
    @Value("${apollo.namespaceName}")
    private String namespaceName;


    @Override
    public List<ParamFlowRuleEntity> getRules(String appName){
        String flowDataId = ApolloConfigUtil.getParamFlowDataId(appName);
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, env, clusterName, namespaceName);
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(flowDataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }

        List<ParamFlowRuleCorrectEntity> entityList = converter.convert(rules);
        return entityList.stream().map(rule -> {
            ParamFlowRule paramFlowRule = new ParamFlowRule();
            BeanUtils.copyProperties(rule, paramFlowRule);
            ParamFlowRuleEntity entity = ParamFlowRuleEntity.fromParamFlowRule(rule.getApp(), rule.getIp(), rule.getPort(), paramFlowRule);
            entity.setId(rule.getId());
            return entity;
        }).collect(Collectors.toList());
    }

}
