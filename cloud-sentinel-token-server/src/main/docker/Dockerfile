FROM apache/skywalking-java-agent:8.12.0-java11

MAINTAINER zhoushuai zs11195310@163.com

ENV SW_AGENT_NAME cloud-sentinel-token-server

ADD cloud-sentinel-token-server.jar /app/cloud-sentinel-token-server.jar

EXPOSE 8769  18730

CMD java $JAVA_TOOL_OPTIONS $JAVA_OPTS -jar /app/cloud-sentinel-token-server.jar
