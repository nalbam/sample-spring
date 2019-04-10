# Dockerfile

FROM maven as BUILD
COPY . /data/src/
RUN mvn -f /data/src/pom.xml package -DskipTests

FROM openjdk:8-jre-alpine
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
RUN apk add --no-cache bash curl
EXPOSE 8080
WORKDIR /data
ENTRYPOINT exec java $JAVA_OPTS -jar ROOT.jar
COPY --from=BUILD /data/src/target/*.jar /data/ROOT.jar
