package com.cloud.sentinel.dashboard.rule.apollo;

import java.util.Objects;

/**
 * @program: sentinel-parent
 * @description:
 * @author: 01398395
 * @create: 2020-07-21 10:20
 **/
public final class ApolloConfigUtil {

    /**
     * 网关-api分组id
     */
    public static final String GATEWAY_API_GROUP_DATA_ID_POSTFIX = "gw-api-group-rules";
    /**
     * 网关-流控规则id
     */
    public static final String GATEWAY_FLOW_DATA_ID_POSTFIX = "gw-flow-rules";
    /**
     * 流控规则id
     */
    public static final String FLOW_DATA_ID_POSTFIX = "flow-rules";
    /**
     * 降级规则id
     */
    public static final String DEGRADE_DATA_ID_POSTFIX = "degrade-rules";
    /**
     * 热点规则id
     */
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "param-flow-rules";
    /**
     * 系统规则id
     */
    public static final String SYSTEM_DATA_ID_POSTFIX = "system-rules";
    /**
     * 授权规则id
     */
    public static final String AUTHORITY_DATA_ID_POSTFIX = "authority-rules";
    /**
     * 集群流控id
     */
    public static final String CLUSTER_GROUP_DATA_ID_POSTFIX = "cluster-group-rules";
    /**
     * 网关appName
     */
    public static final String GATEWAY_APP_NAME = "gateway-server";


    private ApolloConfigUtil() {

    }

    public static String getGatewayFlowDataId(String appName) {
        return String.format("%s_%s", appName, GATEWAY_FLOW_DATA_ID_POSTFIX);
    }

    public static String getGatewayApiGroupDataId(String appName) {
        return String.format("%s_%s", appName, GATEWAY_API_GROUP_DATA_ID_POSTFIX);
    }

    public static String getClusterGroupDataId(String appName) {
        return String.format("%s_%s", appName, CLUSTER_GROUP_DATA_ID_POSTFIX);
    }

    public static String getFlowDataId(String appName) {
        return String.format("%s_%s", appName, FLOW_DATA_ID_POSTFIX);
    }

    public static String getDegradeDataId(String appName) {
        return String.format("%s_%s", appName, DEGRADE_DATA_ID_POSTFIX);
    }

    public static String getParamFlowDataId(String appName) {
        return String.format("%s_%s", appName, PARAM_FLOW_DATA_ID_POSTFIX);
    }

    public static String getSystemDataId(String appName) {
        return String.format("%s_%s", appName, SYSTEM_DATA_ID_POSTFIX);
    }

    public static String getAuthorityDataId(String appName) {
        return String.format("%s_%s", appName, AUTHORITY_DATA_ID_POSTFIX);
    }

    public static Boolean isGatewayAppName(String appName) {
        return Objects.equals(appName, GATEWAY_APP_NAME);
    }

}
