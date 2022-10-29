package com.cloud.sentinel.dashboard.rule.apollo.auth;

import com.cloud.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.correct.AuthorityRuleCorrectEntity;
import com.cloud.sentinel.dashboard.rule.DynamicRuleProvider;
import com.cloud.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
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
 * @description: 授权规则
 * @author: 01398395
 * @create: 2020-07-21 16:57
 **/
@Component("authorityRuleApolloProvider")
public class AuthorityRuleApolloProvider implements DynamicRuleProvider<List<AuthorityRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<String, List<AuthorityRuleCorrectEntity>> converter;
    @Value("${app.id}")
    private String appId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${apollo.clusterName}")
    private String clusterName;
    @Value("${apollo.namespaceName}")
    private String namespaceName;


    @Override
    public List<AuthorityRuleEntity> getRules(String appName){
        String flowDataId = ApolloConfigUtil.getAuthorityDataId(appName);
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
        List<AuthorityRuleCorrectEntity> entityList = converter.convert(rules);
        return entityList.stream().map(rule -> {
            AuthorityRule authorityRule = new AuthorityRule();
            BeanUtils.copyProperties(rule, authorityRule);
            AuthorityRuleEntity entity = AuthorityRuleEntity.fromAuthorityRule(rule.getApp(), rule.getIp(), rule.getPort(), authorityRule);
            entity.setId(rule.getId());
            return entity;
        }).collect(Collectors.toList());
    }

}
