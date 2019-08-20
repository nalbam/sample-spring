# Dockerfile

FROM openjdk:8-jre-alpine

# RUN apk add --no-cache bash curl

ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

ENV TRACER_OPTS=""

# ENV TRACER_OPTS="$TRACER_OPTS -javaagent:dd-java-agent.jar"
# COPY ./tracer/dd-java-agent-0.29.1.jar.zip /data/dd-java-agent.jar

# ENV TRACER_OPTS="$TRACER_OPTS -javaagent:newrelic.jar"
# COPY ./tracer/newrelic.jar.zip /data/newrelic.jar
# COPY ./tracer/newrelic.yml /data/newrelic.yml

# ENV TRACER_OPTS="$TRACER_OPTS -javaagent:whatap.agent.tracer-1.7.7.jar -Dwhatap.home=/data -Dwhatap.micro.enabled=true"
# COPY ./tracer/whatap.agent.tracer-1.7.7.jar.zip /data/whatap.agent.tracer-1.7.7.jar
# COPY ./tracer/whatap.conf /data/whatap.conf

COPY ./target/*.jar /data/ROOT.jar

EXPOSE 8080
WORKDIR /data

ENTRYPOINT exec java $JAVA_OPTS $TRACER_OPTS -jar ROOT.jar
