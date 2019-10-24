# Dockerfile

FROM openjdk:8-jre-alpine
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
# RUN apk add --no-cache bash curl
EXPOSE 8080
WORKDIR /data
ENTRYPOINT exec java $JAVA_OPTS -jar ROOT.jar
COPY target/*.jar /data/ROOT.jar
