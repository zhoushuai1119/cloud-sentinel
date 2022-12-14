package com.cloud.sentinel.token.server.cluster;

import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.cloud.dingtalk.dinger.DingerSender;
import com.cloud.sentinel.token.server.apollo.ApolloClusterConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ApolloClusterConfigManager apolloClusterConfigManager;

    @Autowired
    private DingerSender dingerSender;

    @Value("${zookeeper.address}")
    private String zkAddress;

    private static final String LOCK_PATH = "/tokenServer";

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

        log.info("[sentinel token server]???????????????port: {}", tokenServerPort);

        //??????token server master

        ZK_CLIENT = buildZkClient();
        TOKEN_SERVER_CLIENT = new TokenServerClient(ZK_CLIENT, LOCK_PATH);

        ZK_CLIENT.start();
        TOKEN_SERVER_CLIENT.start();

        //??????1?????????????????????????????????master?????????apollo??????
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            //??????true?????????????????????leader
            boolean hasLeaderShip = TOKEN_SERVER_CLIENT.getLeaderLatch().hasLeadership();
            if (hasLeaderShip) {
                try {
                    String currentIp = HostNameUtil.getIp();
                    if (log.isDebugEnabled()) {
                        log.debug("[Leader????????????]" + currentIp + ",?????????TokenServer Master,??????:" + tokenServerPort);
                    }
                    apolloClusterConfigManager.changeMasterTokenServerAddress(currentIp, tokenServerPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000 * 30, 1000 * 60, TimeUnit.MILLISECONDS);

        //???jvm?????????????????????????????????????????????????????????????????????addShutdownHook???????????????
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CloseableUtils.closeQuietly(ZK_CLIENT);
            CloseableUtils.closeQuietly(TOKEN_SERVER_CLIENT);
        }));
    }

    private CuratorFramework buildZkClient() {
        return CuratorFrameworkFactory.builder()
                //????????????????????????host1:port1,host2:port2
                .connectString(zkAddress)
                //?????????????????????????????????
                .sessionTimeoutMs(5000)
                //???????????????????????????????????????
                .connectionTimeoutMs(5000)
                //????????????
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                //????????????: ????????????????????????????????????????????????Zookeeper????????????????????????????????????????????????????????????????????????????????????
                .namespace("sentinel")
                .build();
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
                    log.info("Leader??????;???" + currentIp + "????????????TokenServer Master,??????:" + tokenServerPort);
                    //????????????????????????
                    dingerSender.send("Leader??????;???" + currentIp + "????????????TokenServer Master,??????:" + tokenServerPort);
                    apolloClusterConfigManager.changeMasterTokenServerAddress(currentIp, tokenServerPort);
                }

                @Override
                public void notLeader() {
                    log.info("???" + name + "????????????master");
                    //????????????????????????
                    dingerSender.send("???" + name + "????????????master");
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
