package com.cloud.sentinel.token.server.utils;

/**
 * @program: sentinel-parent
 * @description:
 * @author: 01398395
 * @create: 2020-07-21 10:20
 **/
public final class ApolloConfigUtil {

    /**
     * sentinel限流规则配置namespace
     */
    private static final String SENTINEL_RULES_NAMESPACE = "development.sentinel-rules";

    /**
     * token server namespace
     */
    private static final String TOKEN_SERVER_NAMESPACE = "development.token-server";

    /**
     * token server rule key
     */
    private static final String TOKEN_SERVER_RULE_KEY = "token-server-cluster-map";

    /**
     * token server namespace set
     */
    private static final String TOKEN_SERVER_NAMESPACE_SET_KEY = "token-server-namespace-set";

    /**
     * 流控规则id
     */
    public static final String FLOW_DATA_ID_POSTFIX = "flow-rules";

    /**
     * 热点规则id
     */
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "param-flow-rules";


    public static String getFlowDataId(String appName) {
        return String.format("%s_%s", appName, FLOW_DATA_ID_POSTFIX);
    }


    public static String getParamFlowDataId(String appName) {
        return String.format("%s_%s", appName, PARAM_FLOW_DATA_ID_POSTFIX);
    }

    public static String getSentinelRulesNamespace() {
        return SENTINEL_RULES_NAMESPACE;
    }

    public static String getTokenServeNamespace() {
        return TOKEN_SERVER_NAMESPACE;
    }

    public static String getTokenServerRuleKey() {
        return TOKEN_SERVER_RULE_KEY;
    }


    public static String getTokenServerNamespaceSetKey() {
        return TOKEN_SERVER_NAMESPACE_SET_KEY;
    }

}
