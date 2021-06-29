#!/bin/sh

if [ "${CG_AGENT_ENABLED}" == "true" ]; then
    export JAVA_AGENT_PATH="/data/codeguru-profiler-java-agent.jar"
    export JAVA_OPTS="${JAVA_OPTS} -javaagent:${JAVA_AGENT_PATH}"
fi

if [ "${DD_AGENT_ENABLED}" == "true" ]; then
    export JAVA_AGENT_PATH="/data/dd-java-agent.jar"
    export JAVA_OPTS="${JAVA_OPTS} -javaagent:${JAVA_AGENT_PATH}"
fi

if [ "${JAVA_AGENT_ENABLED}" == "true" ]; then
    export JAVA_AGENT_CONFIG="/data/config.yaml"
    export JAVA_AGENT_PATH="/data/jmx_javaagent.jar"
    export JAVA_AGENT_PORT="8081"
    export JAVA_OPTS="${JAVA_OPTS} -javaagent:${JAVA_AGENT_PATH}=${JAVA_AGENT_PORT}:${JAVA_AGENT_CONFIG}"
fi

java ${JAVA_OPTS} -jar /data/ROOT.jar
