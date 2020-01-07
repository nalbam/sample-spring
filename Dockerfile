# Dockerfile

FROM openjdk:8-jre-alpine

# RUN apk add --no-cache bash curl

EXPOSE 8080
EXPOSE 8088

ENTRYPOINT ["/entrypoint.sh"]

COPY target/entrypoint.sh /entrypoint.sh

COPY target/jmx/*.jar /jmx_javaagent.jar
COPY target/jmx/config.yaml /config.yaml

COPY target/*.jar /ROOT.jar
