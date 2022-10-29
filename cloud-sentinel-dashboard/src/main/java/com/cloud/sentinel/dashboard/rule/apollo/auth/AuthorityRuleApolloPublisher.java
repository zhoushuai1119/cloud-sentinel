package com.cloud.sentinel.dashboard.rule.apollo.auth;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSON;
import com.cloud.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.cloud.sentinel.dashboard.datasource.entity.rule.correct.AuthorityRuleCorrectEntity;
import com.cloud.sentinel.dashboard.rule.DynamicRulePublisher;
import com.cloud.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: sentinel-parent
 * @description: 授权规则
 * @author: 01398395
 * @create: 2020-07-21 17:00
 **/
@Component("authorityRuleApolloPublisher")
@Slf4j
public class AuthorityRuleApolloPublisher implements DynamicRulePublisher<List<AuthorityRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;
    @Autowired
    private Converter<List<AuthorityRuleCorrectEntity>, String> converter;
    @Value("${app.id}")
    private String appId;
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${apollo.user}")
    private String user;
    @Value("${apollo.clusterName}")
    private String clusterName;
    @Value("${apollo.namespaceName}")
    private String namespaceName;


    @Override
    public void publish(String app, List<AuthorityRuleEntity> rules) {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        filterField(rules);

        //  转换
        List<AuthorityRuleCorrectEntity> authorityRuleList = rules.stream().map(rule -> {
            AuthorityRuleCorrectEntity entity = new AuthorityRuleCorrectEntity();
            BeanUtils.copyProperties(rule,entity);
            return entity;
        }).collect(Collectors.toList());

        // Increase the configuration
        String flowDataId = ApolloConfigUtil.getAuthorityDataId(app);
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(converter.convert(authorityRuleList));
        openItemDTO.setComment(app + "授权规则");
        openItemDTO.setDataChangeCreatedBy(user);
        apolloOpenApiClient.createOrUpdateItem(appId, env, clusterName, namespaceName, openItemDTO);

        // 发布配置
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        namespaceReleaseDTO.setReleasedBy(user);
        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(appId, env, clusterName, namespaceName, namespaceReleaseDTO);

        log.info("set app : {} AuthorityRule success rules: {}", app, JSON.toJSONString(rules));
    }

    /**
     * 过滤不必要的字段
     *
     * @param rules
     */
    private void filterField(List<AuthorityRuleEntity> rules) {
        // 对不必要的信息进行过滤
        for (AuthorityRuleEntity rule : rules) {
            rule.setGmtCreate(null);
            rule.setGmtModified(null);
            rule.setIp(null);
            rule.setPort(null);
        }
    }

}
