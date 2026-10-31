#!/bin/bash
#
# Starts the image once per Java agent setting and checks that the app and the
# selected agents start:
#
#   ./scripts/smoke-agents.sh sample-spring:ci
#
# Cases: no agent, JAVA_AGENT_ENABLED (JMX Exporter on 8081), DD_AGENT_ENABLED,
# CG_AGENT_ENABLED, and all three together with JAVA_OPTS. Each case checks /,
# /health, /live, /actuator/health and /actuator/prometheus on 8080.
#
# Nothing is sent to Datadog or AWS: no Datadog agent or API key is configured
# (the tracer only tries localhost:8126 inside the container), and the
# CodeGuru profiler runs without AWS credentials and with EC2 metadata disabled,
# so it stops at credential lookup. The checks prove agent startup only, not
# delivery to the collection services.

set -euo pipefail

IMAGE="${1:?usage: $0 IMAGE}"
NAME="sample-spring-smoke"
TIMEOUT="${SMOKE_TIMEOUT:-90}"

DD_VERSION="1.68.0"
JMX_VERSION="1.7.0"

cleanup() {
  docker rm -f "${NAME}" > /dev/null 2>&1 || true
}
trap cleanup EXIT

fail() {
  echo "FAIL [${CASE}]: $*" >&2
  docker logs "${NAME}" >&2 || true
  exit 1
}

wait_for() {
  local url="$1"
  for _ in $(seq 1 "${TIMEOUT}"); do
    if curl -fsS -o /dev/null "${url}"; then
      return 0
    fi
    if [ "$(docker inspect -f '{{.State.Running}}' "${NAME}" 2>/dev/null)" != "true" ]; then
      fail "container exited"
    fi
    sleep 1
  done
  fail "no response from ${url} in ${TIMEOUT}s"
}

# run_case NAME [docker run -e options...]
run_case() {
  CASE="$1"
  shift
  cleanup
  docker run -d --name "${NAME}" -p 8080:8080 -p 8081:8081 \
    -e SPRING_PROFILES_ACTIVE=default "$@" "${IMAGE}" > /dev/null

  wait_for "http://localhost:8080/health"
  for path in / /health /live /actuator/health; do
    curl -fsS -o /dev/null "http://localhost:8080${path}" || fail "GET ${path}"
  done
  local metrics
  metrics="$(curl -fsS "http://localhost:8080/actuator/prometheus")" || fail "GET /actuator/prometheus"
  grep -q '^jvm_' <<< "${metrics}" || fail "no jvm_ metrics on /actuator/prometheus"

  LOGS="$(docker logs "${NAME}" 2>&1)"
}

expect_jmx() {
  local metrics
  metrics="$(curl -fsS "http://localhost:8081/metrics")" || fail "GET :8081/metrics"
  grep -q "^jmx_exporter_build_info{.*version=\"${JMX_VERSION}\"" <<< "${metrics}" ||
    fail "JMX Exporter ${JMX_VERSION} not found on :8081/metrics"
  grep -q '^jvm_' <<< "${metrics}" || fail "no jvm_ metrics on :8081/metrics"
}

expect_no_jmx() {
  if curl -fsS -o /dev/null "http://localhost:8081/metrics" 2>/dev/null; then
    fail ":8081/metrics answers without JAVA_AGENT_ENABLED"
  fi
}

expect_dd() {
  grep -q "DATADOG TRACER CONFIGURATION {\"version\":\"${DD_VERSION}" <<< "${LOGS}" ||
    fail "Datadog tracer ${DD_VERSION} did not start"
}

expect_cg() {
  grep -q "Starting the profiler" <<< "${LOGS}" || fail "CodeGuru profiler did not start"
}

expect_no_agent_logs() {
  if grep -qE "DATADOG TRACER|codeguruprofilerjavaagent" <<< "${LOGS}"; then
    fail "agent started without its *_AGENT_ENABLED variable"
  fi
}

CG_ENV=(
  -e CG_AGENT_ENABLED=true
  -e AWS_CODEGURU_PROFILER_GROUP_NAME=smoke-local
  -e AWS_CODEGURU_PROFILER_TARGET_REGION=us-east-1
  -e AWS_EC2_METADATA_DISABLED=true
)

run_case none
expect_no_jmx
expect_no_agent_logs
echo "PASS [${CASE}]"

run_case jmx -e JAVA_AGENT_ENABLED=true
expect_jmx
expect_no_agent_logs
echo "PASS [${CASE}]"

run_case datadog -e DD_AGENT_ENABLED=true
expect_dd
expect_no_jmx
echo "PASS [${CASE}]"

run_case codeguru "${CG_ENV[@]}"
expect_cg
expect_no_jmx
echo "PASS [${CASE}]"

run_case all "${CG_ENV[@]}" -e DD_AGENT_ENABLED=true -e JAVA_AGENT_ENABLED=true \
  -e JAVA_OPTS="-Dsmoke.java.opts=true"
PROCESSES="$(docker exec "${NAME}" ps -o args)"
grep -q -- "-Dsmoke.java.opts=true" <<< "${PROCESSES}" || fail "JAVA_OPTS not passed to java"
expect_cg
expect_dd
expect_jmx
echo "PASS [${CASE}]"
