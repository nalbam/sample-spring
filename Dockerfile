# Dockerfile

FROM openjdk:8-jre-alpine
# RUN apk add --no-cache bash curl
# ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENV JAVA_OPTS="-javaagent:jmx_prometheus_javaagent-0.11.0.jar=8081:config.yaml"
EXPOSE 8080
WORKDIR /data
ENTRYPOINT exec java $JAVA_OPTS -jar ROOT.jar
COPY jmx/*.jar /data/jmx.jar
COPY jmx/config.yaml /data/config.yaml
COPY target/*.jar /data/ROOT.jar
