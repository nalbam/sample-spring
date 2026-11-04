# syntax=docker/dockerfile:1

# build: compiles, tests and packages the jar on the build platform.
FROM --platform=$BUILDPLATFORM maven:3-eclipse-temurin-26 AS build
WORKDIR /src
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B verify && mkdir /out && cp target/sample-spring-*.jar /out/sample-spring.jar

# artifact: the release artifact, exported by scripts/release.sh artifact.
FROM scratch AS artifact
COPY --from=build /out /

FROM eclipse-temurin:27-jre-alpine

LABEL org.opencontainers.image.title="sample-spring" \
      org.opencontainers.image.description="A sample Spring Boot app for Kubernetes demos" \
      org.opencontainers.image.source="https://github.com/nalbam/sample-spring"

WORKDIR /data

# Java agents, enabled by entrypoint.sh. The build fails if a download does not
# match its SHA-256 (published by Maven Central or the GitHub release).
# https://central.sonatype.com/artifact/software.amazon.codeguruprofiler/codeguru-profiler-java-agent-standalone
ADD --chmod=644 --checksum=sha256:dbd334095d066ae2809f28f646bc9b730859c97110986c4ff0f8f0d103c5af8e \
    https://repo1.maven.org/maven2/software/amazon/codeguruprofiler/codeguru-profiler-java-agent-standalone/1.2.7/codeguru-profiler-java-agent-standalone-1.2.7.jar \
    /data/codeguru-profiler-java-agent.jar
# https://central.sonatype.com/artifact/com.datadoghq/dd-java-agent
ADD --chmod=644 --checksum=sha256:b553475e2f00726cb8a7cd1c227dbe9236d4877869a85d57f4077cbc326332f4 \
    https://repo1.maven.org/maven2/com/datadoghq/dd-java-agent/1.68.0/dd-java-agent-1.68.0.jar \
    /data/dd-java-agent.jar
# https://github.com/prometheus/jmx_exporter/releases/tag/1.7.0
ADD --chmod=644 --checksum=sha256:0ad843261c567d1924a017d542e14c40f2afff523d940c25ee000742fde4b3a9 \
    https://github.com/prometheus/jmx_exporter/releases/download/1.7.0/jmx_prometheus_javaagent-1.7.0.jar \
    /data/jmx_javaagent.jar

COPY jmx/config.yaml /data/config.yaml
COPY entrypoint.sh /data/entrypoint.sh
COPY --from=artifact /sample-spring.jar /data/ROOT.jar
RUN adduser -D -H -u 10001 app

USER 10001

ARG VERSION=v0.0.0-dev
ENV PORT=8080 \
    VERSION=${VERSION}

# 8080: HTTP. 8081: JMX exporter when JAVA_AGENT_ENABLED=true.
EXPOSE 8080 8081

ENTRYPOINT ["/bin/sh", "/data/entrypoint.sh"]
