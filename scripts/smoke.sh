#!/usr/bin/env bash
#
# Smoke test for the sample-spring image. CI and Jenkins run it after the build.
#
#   scripts/smoke.sh IMAGE [VERSION]
#
# Starts IMAGE with VERSION and checks it from a curl container in the same
# network namespace, so the host needs only docker and jq. Exits non-zero and
# prints the container log when a check fails. The helpers above the
# "sample-spring checks" line are the same in every sample-* repository.

# Each repository uses a subset of the shared helpers, may replace some of them,
# and may call check_port without arguments.
# shellcheck disable=SC2119,SC2120,SC2317,SC2329

set -euo pipefail

IMAGE="${1:?usage: scripts/smoke.sh IMAGE [VERSION]}"
VERSION="${2:-v0.0.0-smoke}"
PORT=8080
TIMEOUT="${SMOKE_TIMEOUT:-90}"
CURL_IMAGE=curlimages/curl:8.23.0
NAME="smoke-$$-$RANDOM"

cleanup() { docker rm -f "$NAME" > /dev/null 2>&1 || true; }
trap cleanup EXIT

# Pull once, so a pull error shows here instead of as a failed health check.
docker pull -q "$CURL_IMAGE" > /dev/null

fail() {
  echo "FAIL: $*" >&2
  docker logs "$NAME" >&2 || true
  exit 1
}

# http CURL_ARGS... runs curl in the network namespace of the container.
http() { docker run --rm --network "container:$NAME" "$CURL_IMAGE" -sS --max-time 10 "$@"; }

# ready succeeds when the app answers its health check. sample-grpc replaces it.
ready() { http -f -o /dev/null "http://localhost:$PORT/health" 2> /dev/null; }

# start [DOCKER_RUN_ARGS...] starts the image and waits until it is ready.
start() {
  cleanup
  docker run -d --name "$NAME" -e VERSION="$VERSION" "$@" "$IMAGE" > /dev/null
  local deadline=$((SECONDS + TIMEOUT))
  while [ "$SECONDS" -lt "$deadline" ]; do
    [ "$(docker inspect -f '{{.State.Running}}' "$NAME")" = true ] || fail "container exited"
    ready && return 0
    sleep 1
  done
  fail "the health check did not pass in ${TIMEOUT}s"
}

# check_health checks the /health contract: HTTP 200, result "ok" and VERSION.
# sample-grpc replaces it.
check_health() {
  local body
  body="$(http -f "http://localhost:$PORT/health")" || fail "GET /health"
  jq -e --arg version "$VERSION" '.result == "ok" and .version == $version' <<< "$body" > /dev/null ||
    fail "GET /health returned $body, expected result ok and version $VERSION"
  echo "ok: GET /health $body"
}

# check_get PATH [TEXT] checks HTTP 200 and, when TEXT is given, that the body contains it.
check_get() {
  if [ -z "${2:-}" ]; then
    http -f -o /dev/null "http://localhost:$PORT$1" || fail "GET $1"
    echo "ok: GET $1"
  else
    http -f "http://localhost:$PORT$1" | grep -F -- "$2" > /dev/null || fail "GET $1 does not contain: $2"
    echo "ok: GET $1 contains: $2"
  fi
}

# check_status PATH CODE checks the HTTP status code.
check_status() {
  local code
  code="$(http -o /dev/null -w '%{http_code}' "http://localhost:$PORT$1")" || fail "GET $1"
  [ "$code" = "$2" ] || fail "GET $1 returned HTTP $code, expected $2"
  echo "ok: GET $1 HTTP $code"
}

# check_non_root checks that the main process does not run as root.
check_non_root() {
  local uid
  uid="$(docker exec "$NAME" id -u)"
  [ "$uid" != 0 ] || fail "the container runs as root"
  echo "ok: runs as uid $uid"
}

# check_port checks that the image listens on another PORT.
check_port() {
  PORT=18080
  start -e PORT="$PORT" "$@"
  check_health
  echo "ok: listens on PORT=$PORT"
}

# --- sample-spring checks ----------------------------------------------------------

# check_stop checks that docker stop ends Java with SIGTERM (exit code 143), not SIGKILL (137).
check_stop() {
  docker stop --time 30 "$NAME" > /dev/null
  local code
  code="$(docker inspect -f '{{.State.ExitCode}}' "$NAME")"
  [ "$code" = 0 ] || [ "$code" = 143 ] || fail "exit code $code after docker stop, expected 0 or 143"
  echo "ok: docker stop exits with code $code"
}

# Java agents. Nothing is sent to Datadog or AWS: no Datadog agent or API key is
# set, and the CodeGuru profiler stops at the credential lookup. The checks prove
# that the agents start, not that data is delivered.
DD_VERSION=1.68.0
JMX_VERSION=1.7.0

check_jmx() {
  local metrics
  metrics="$(http -f "http://localhost:8081/metrics")" || fail "GET :8081/metrics"
  grep -q "^jmx_exporter_build_info{.*version=\"$JMX_VERSION\"" <<< "$metrics" ||
    fail "JMX exporter $JMX_VERSION not found on :8081/metrics"
  echo "ok: JMX exporter $JMX_VERSION on :8081"
}

check_no_jmx() {
  if http -f -o /dev/null "http://localhost:8081/metrics" 2> /dev/null; then
    fail ":8081/metrics answers without JAVA_AGENT_ENABLED"
  fi
  echo "ok: no JMX exporter"
}

check_dd() {
  docker logs "$NAME" 2>&1 | grep -F "DATADOG TRACER CONFIGURATION {\"version\":\"$DD_VERSION" > /dev/null ||
    fail "Datadog tracer $DD_VERSION did not start"
  echo "ok: Datadog tracer $DD_VERSION started"
}

check_no_agent_logs() {
  if docker logs "$NAME" 2>&1 | grep -E "DATADOG TRACER|codeguruprofilerjavaagent" > /dev/null; then
    fail "an agent started without its *_AGENT_ENABLED variable"
  fi
  echo "ok: no agent started"
}

check_cg() {
  docker logs "$NAME" 2>&1 | grep -F "Starting the profiler" > /dev/null || fail "CodeGuru profiler did not start"
  echo "ok: CodeGuru profiler started"
}

CG_ENV=(
  -e CG_AGENT_ENABLED=true
  -e AWS_CODEGURU_PROFILER_GROUP_NAME=smoke-local
  -e AWS_CODEGURU_PROFILER_TARGET_REGION=us-east-1
  -e AWS_EC2_METADATA_DISABLED=true
)

start
check_health
check_get / "<p>$VERSION</p>"
for path in /live /read /actuator/health; do
  check_get "$path"
done
check_get /actuator/prometheus 'jvm_'
check_non_root
check_no_jmx
check_no_agent_logs
check_stop

start -e JAVA_AGENT_ENABLED=true
check_jmx
check_no_agent_logs

start -e DD_AGENT_ENABLED=true
check_dd
check_no_jmx

start "${CG_ENV[@]}"
check_cg
check_no_jmx

start "${CG_ENV[@]}" -e DD_AGENT_ENABLED=true -e JAVA_AGENT_ENABLED=true -e JAVA_OPTS=-Dsmoke.java.opts=true
docker exec "$NAME" ps -o args | grep -F -- "-Dsmoke.java.opts=true" > /dev/null || fail "JAVA_OPTS not passed to java"
echo "ok: JAVA_OPTS passed to java"
check_cg
check_dd
check_jmx

check_port

echo "smoke test passed: $IMAGE $VERSION"
