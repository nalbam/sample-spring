# Dockerfile

FROM openjdk:8-jre-alpine
# RUN apk add --no-cache bash curl
# ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
ENV JAVA_OPTS="-javaagent:jmx_javaagent.jar=8088:config.yaml"
EXPOSE 8080
EXPOSE 8088
WORKDIR /data
ENTRYPOINT exec java $JAVA_OPTS -jar ROOT.jar
COPY jmx/config.yaml /data/config.yaml
COPY jmx/*.jar /data/jmx_javaagent.jar
COPY target/*.jar /data/ROOT.jar
