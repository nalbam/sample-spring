# Dockerfile

# FROM openjdk:8-jre-slim-buster
FROM openjdk:17-jdk-alpine

LABEL maintainer="me@nalbam.com" \
      org.opencontainers.image.description="A Sample Docker image for Spring App" \
      org.opencontainers.image.authors="Jungyoul Yu, me@nalbam.com, https://www.nalbam.com/" \
      org.opencontainers.image.source="https://github.com/nalbam/sample-spring" \
      org.opencontainers.image.title="sample-spring"

# RUN apk add --no-cache bash curl

EXPOSE 8080
EXPOSE 8081

WORKDIR /data

COPY target/*.jar /data/ROOT.jar

COPY ./entrypoint.sh /data/entrypoint.sh

COPY ./jmx/config.yaml /data/config.yaml
COPY ./jmx/dd-java-agent-0.75.0.jar.zip /data/dd-java-agent.jar
COPY ./jmx/jmx_prometheus_javaagent-0.15.0.jar.zip /data/jmx_javaagent.jar
COPY ./jmx/codeguru-profiler-java-agent-standalone-1.1.1.jar.zip /data/codeguru-profiler-java-agent.jar

ENTRYPOINT ["/bin/sh", "entrypoint.sh"]
