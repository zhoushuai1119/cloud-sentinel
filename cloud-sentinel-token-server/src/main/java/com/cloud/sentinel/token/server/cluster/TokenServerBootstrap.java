package com.cloud.sentinel.token.server.cluster;

import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author zhoushuai
 */
@Component
@Slf4j
public class TokenServerBootstrap {

    @PostConstruct
    public void init() throws Exception {
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();
        // Start the server.
        tokenServer.start();
        Integer tokenServerPort = ClusterServerConfigManager.getPort();
        log.info("[sentinel token server]启动成功，port: {}", tokenServerPort);
    }

}
