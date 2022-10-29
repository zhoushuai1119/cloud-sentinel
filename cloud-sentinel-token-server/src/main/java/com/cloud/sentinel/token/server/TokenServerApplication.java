package com.cloud.sentinel.token.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author shuai.zhou
 *
 * 启动类加上启动参数:
 * -javaagent:E:\develop\SkyWalking\skywalking-agent\skywalking-agent.jar
 * -Dskywalking.agent.service_name=sentinel-token-server
 * -Dskywalking.collector.backend_service=139.196.208.53:11800
 * -Dcsp.sentinel.log.use.pid=true
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class TokenServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenServerApplication.class, args);
    }

}
