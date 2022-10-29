FROM apache/skywalking-java-agent:8.12.0-java11

MAINTAINER zhoushuai zs11195310@163.com

ENV SW_AGENT_NAME cloud-sentinel-dashboard

ADD cloud-sentinel-dashboard.jar /app/cloud-sentinel-dashboard.jar

EXPOSE 9999

CMD java $JAVA_TOOL_OPTIONS $JAVA_OPTS -jar /app/cloud-sentinel-dashboard.jar
