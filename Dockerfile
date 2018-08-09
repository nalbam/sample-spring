# Dockerfile

FROM openjdk:8-jre-slim

ENV TZ Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    ls -al target

EXPOSE 8080

COPY target/*.jar /data/ROOT.jar

WORKDIR data

CMD ["java", "-jar", "ROOT.jar"]
