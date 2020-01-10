# Dockerfile

FROM openjdk:8-jre-alpine

# RUN apk add --no-cache bash curl

EXPOSE 8080
EXPOSE 8081

WORKDIR /data

ENTRYPOINT ["/data/entrypoint.sh"]

COPY target/entrypoint.sh /data/entrypoint.sh

COPY target/jmx/*.jar /data/jmx_javaagent.jar
COPY target/jmx/config.yaml /data/config.yaml

COPY target/*.jar /data/ROOT.jar
