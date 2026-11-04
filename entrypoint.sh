#!/bin/sh

# Each agent has its own path variable, so the agents can run together.
# JAVA_AGENT_PATH belongs to the JMX exporter only.

if [ "${CG_AGENT_ENABLED}" = "true" ]; then
    export CG_AGENT_PATH="${CG_AGENT_PATH:-/data/codeguru-profiler-java-agent.jar}"
    export JAVA_OPTS="${JAVA_OPTS} -javaagent:${CG_AGENT_PATH}"
fi

if [ "${DD_AGENT_ENABLED}" = "true" ]; then
    export DD_AGENT_PATH="${DD_AGENT_PATH:-/data/dd-java-agent.jar}"
    export JAVA_OPTS="${JAVA_OPTS} -javaagent:${DD_AGENT_PATH}"
fi

if [ "${JAVA_AGENT_ENABLED}" = "true" ]; then
    export JAVA_AGENT_CONFIG="${JAVA_AGENT_CONFIG:-/data/config.yaml}"
    export JAVA_AGENT_PATH="${JAVA_AGENT_PATH:-/data/jmx_javaagent.jar}"
    export JAVA_AGENT_PORT="${JAVA_AGENT_PORT:-8081}"
    export JAVA_OPTS="${JAVA_OPTS} -javaagent:${JAVA_AGENT_PATH}=${JAVA_AGENT_PORT}:${JAVA_AGENT_CONFIG}"
fi

# exec makes Java PID 1, so it receives SIGTERM from docker stop and Kubernetes.
# shellcheck disable=SC2086 # JAVA_OPTS holds several options.
exec java ${JAVA_OPTS} -jar /data/ROOT.jar
