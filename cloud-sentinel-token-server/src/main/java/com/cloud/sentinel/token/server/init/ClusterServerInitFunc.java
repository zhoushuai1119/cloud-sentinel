package com.cloud.sentinel.token.server.init;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerFlowConfig;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import com.cloud.sentinel.token.server.utils.ApolloConfigUtil;
import com.cloud.sentinel.token.server.parser.ClusterServerFlowConfigParser;
import com.cloud.sentinel.token.server.parser.TokenServerTransportConfigParser;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: zhou shuai
 * @date: 2022/10/24 15:50
 * @version: v1
 *
 * 启动类加上启动参数:
 * -Dcsp.sentinel.log.use.pid=true
 * -Dproject.name=token-server
 * -Dcsp.sentinel.dashboard.server=127.0.0.1:9999
 */
@Slf4j
public class ClusterServerInitFunc implements InitFunc {

    /**
     * defaultRules
     */
    private String defaultRules = "[]";

    @Override
    public void init() {

        String sentinelRulesNameSpace = ApolloConfigUtil.getSentinelRulesNamespace();

        String tokenServerNameSpace = ApolloConfigUtil.getTokenServeNamespace();

        // 监听特定namespace下的集群限流规则
        initPropertySupplier(sentinelRulesNameSpace);

        // 设置tokenServer管辖的作用域(即管理哪些应用)
        initTokenServerNameSpaces(tokenServerNameSpace);

        // 设置token-server使用哪个端口与token-client通信
        initServerTransportConfig(tokenServerNameSpace);

        // 初始化最大qps
        initServerFlowConfig(tokenServerNameSpace);

        //设置为 token server
        initStateProperty();
    }

    private void initPropertySupplier(String sentinelRulesNameSpace) {
        // Register cluster flow rule property supplier which creates data source by namespace.
        ClusterFlowRuleManager.setPropertySupplier(appName -> {
            ReadableDataSource<String, List<FlowRule>> ds = new ApolloDataSource<>(sentinelRulesNameSpace,
                    ApolloConfigUtil.getFlowDataId(appName), defaultRules, source -> JSON.parseObject(source,
                    new TypeReference<List<FlowRule>>() {
                    }));
            return ds.getProperty();
        });

        // Register cluster parameter flow rule property supplier.
        ClusterParamFlowRuleManager.setPropertySupplier(appName -> {
            ReadableDataSource<String, List<ParamFlowRule>> ds = new ApolloDataSource<>(sentinelRulesNameSpace,
                    ApolloConfigUtil.getParamFlowDataId(appName), defaultRules, source -> JSON.parseObject(source,
                    new TypeReference<List<ParamFlowRule>>() {
                    }));
            return ds.getProperty();
        });
    }

    /**
     * @description: Server namespace set (scope) data source.
     * @author: zhou shuai
     * @param tokenServerNameSpace
     * @date: 2022/10/25 14:40
     */
    private void initTokenServerNameSpaces(String tokenServerNameSpace) {
        ReadableDataSource<String, Set<String>> namespaceDs = new ApolloDataSource<>(tokenServerNameSpace,
                ApolloConfigUtil.getTokenServerNamespaceSetKey(), defaultRules, source -> JSON.parseObject(source,
                new TypeReference<Set<String>>() {
                }));
        log.info("namespaceDs : {}", namespaceDs.getProperty());
        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());
    }

    /**
     * @description: Server transport configuration data source.
     * @author: zhou shuai
     * @param tokenServerNameSpace
     * @date: 2022/10/25 14:39
     */
    private void initServerTransportConfig(String tokenServerNameSpace) {
        ReadableDataSource<String, ServerTransportConfig> serverTransportDs = new ApolloDataSource<>(tokenServerNameSpace,
                ApolloConfigUtil.getTokenServerRuleKey(), defaultRules,
                new TokenServerTransportConfigParser());
        ClusterServerConfigManager.registerServerTransportProperty(serverTransportDs.getProperty());
    }


    /**
     * @description: 初始化最大qps
     * @author: zhou shuai
     * @param tokenServerNameSpace
     * @date: 2022/10/25 14:40
     */
    private void initServerFlowConfig(String tokenServerNameSpace) {
        ClusterServerFlowConfigParser serverFlowConfigParser = new ClusterServerFlowConfigParser();
        ReadableDataSource<String, ServerFlowConfig> serverFlowConfigDs = new ApolloDataSource<>(tokenServerNameSpace,
                ApolloConfigUtil.getTokenServerRuleKey(), defaultRules, s -> {
            ServerFlowConfig config = serverFlowConfigParser.convert(s);
            if (config != null) {
                ClusterServerConfigManager.loadGlobalFlowConfig(config);
            }
            return config;
        });
    }

    /**
     * 设置为 token server
     */
    private void initStateProperty() {
        ClusterStateManager.applyState(ClusterStateManager.CLUSTER_SERVER);
    }

}
