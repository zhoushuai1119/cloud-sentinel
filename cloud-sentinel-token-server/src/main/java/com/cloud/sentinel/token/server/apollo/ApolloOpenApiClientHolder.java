package com.cloud.sentinel.token.server.apollo;

import com.cloud.sentinel.token.server.utils.ApolloConfigUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Value;

/**
 * @description:
 * @author: zhou shuai
 * @date: 2022/11/21 20:02
 * @version: v1
 */
public class ApolloOpenApiClientHolder {

    private static final ApolloOpenApiClient APOLLO_OPEN_API_CLIENT = ApolloOpenApiClient.newBuilder()
            .withPortalUrl(ApolloConfigUtil.getApolloPortalUrl())
            .withToken(ApolloConfigUtil.getApolloToken())
            .build();

    public static ApolloOpenApiClient getApolloOpenApiClient() {
        return APOLLO_OPEN_API_CLIENT;
    }

}
