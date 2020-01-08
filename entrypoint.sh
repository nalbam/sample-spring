#!/bin/bash

# JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"

if [ "${JMX_ENABLED}" == "true" ]; then
    JMX_PATH=${JMX_PATH:-"/jmx_javaagent.jar"}
    JMX_PORT=${JMX_PORT:-8088}
    JMX_CONFIG=${JMX_CONFIG:-"/config.yaml"}

    JAVA_OPTS="${JAVA_OPTS} -javaagent:${JMX_PATH}=${JMX_PORT}:${JMX_CONFIG}"
fi

JAR_PATH=${JAR_PATH:-"/ROOT.jar"}

java ${JAVA_OPTS} -jar ${JAR_PATH}
