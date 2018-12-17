# Dockerfile

FROM openjdk:8-jre-alpine
ENV TZ Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apk add --no-cache bash
EXPOSE 8080
COPY ./target/*.jar /data/ROOT.jar
WORKDIR /data
CMD ["java", "-jar", "ROOT.jar"]
