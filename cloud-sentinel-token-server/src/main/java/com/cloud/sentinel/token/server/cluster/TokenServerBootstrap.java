package com.cloud.sentinel.token.server.cluster;

import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.utils.CloseableUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhoushuai
 */
@Component
@Slf4j
public class TokenServerBootstrap {

    @Value("${zookeeper.address}")
    private String zkAddress;

    private static final String LOCK_PATH = "/sentinel/tokenServer";

    private static CuratorFramework ZK_CLIENT;

    private static TokenServerClient TOKEN_SERVER_CLIENT;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r ->
            new Thread(r, "TokenServerCheckMasterThread"));

    @PostConstruct
    public void init() throws Exception {
        ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();
        // Start the server.
        tokenServer.start();

        Integer tokenServerPort = ClusterServerConfigManager.getPort();

        log.info("[sentinel token server]启动成功，port: {}", tokenServerPort);

        //争抢token server master

        ZK_CLIENT = buildZkClient();
        TOKEN_SERVER_CLIENT = new TokenServerClient(ZK_CLIENT, LOCK_PATH);

        ZK_CLIENT.start();
        TOKEN_SERVER_CLIENT.start();

        //每隔1分钟进行一次自检，防止master地址写apollo失败
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            boolean hasLeaderShip = TOKEN_SERVER_CLIENT.getLeaderLatch().hasLeadership();
            if (hasLeaderShip) {
                try {
                    String currentIp = HostNameUtil.getIp();
                    log.info("[Leader定时检查]" + currentIp + ",当前是TokenServer Master,端口:" + tokenServerPort);
                    ApolloClusterConfigManager.changeMasterTokenServerAddress(currentIp, tokenServerPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000 * 30, 1000 * 60, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CloseableUtils.closeQuietly(ZK_CLIENT);
            CloseableUtils.closeQuietly(TOKEN_SERVER_CLIENT);
        }));
    }

    class TokenServerClient implements Closeable {
        private final String name;
        private final LeaderLatch leaderLatch;

        public TokenServerClient(CuratorFramework client, String path) {
            this.name = HostNameUtil.getIp();
            leaderLatch = new LeaderLatch(client, path);
            leaderLatch.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    String currentIp = HostNameUtil.getIp();
                    Integer tokenServerPort = ClusterServerConfigManager.getPort();
                    log.info("[Leader选举]" + currentIp + ",成为了TokenServer Master,端口:" + tokenServerPort);
                    ApolloClusterConfigManager.changeMasterTokenServerAddress(currentIp, tokenServerPort);

                }

                @Override
                public void notLeader() {
                    log.info(name + "失去了master");
                }
            });
        }

        @Override
        public void close() throws IOException {
            leaderLatch.close(LeaderLatch.CloseMode.NOTIFY_LEADER);
        }

        public void start() {
            try {
                leaderLatch.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public LeaderLatch getLeaderLatch() {
            return leaderLatch;
        }
    }
}
