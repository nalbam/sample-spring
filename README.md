# sample-spring

[![ci](https://img.shields.io/github/actions/workflow/status/nalbam/sample-spring/ci.yml?branch=main&label=ci&style=for-the-badge&logo=github)](https://github.com/nalbam/sample-spring/actions/workflows/ci.yml)
[![release](https://img.shields.io/github/v/release/nalbam/sample-spring?style=for-the-badge&logo=github)](https://github.com/nalbam/sample-spring/releases)

A sample Spring Boot app for Kubernetes demos, with optional Java agents.

## Docker

```bash
docker run -d --name sample-spring -p 8080:8080 ghcr.io/nalbam/sample-spring
until curl -fs http://localhost:8080/health; do sleep 1; done   # the JVM starts in a few seconds
docker rm -f sample-spring
```

The image uses `eclipse-temurin:27-jre-alpine` and runs as the non-root uid `10001`.
`entrypoint.sh` starts Java with `exec`, so Java receives `SIGTERM` from `docker stop`.

| Environment variable | Default | Description |
| --- | --- | --- |
| `PORT` | `8080` | HTTP listen port. |
| `VERSION` | `v0.0.0-dev` | Version that `/health` and `/` report. Release images set it to their tag; a local build sets it with `--build-arg VERSION=...`. |
| `SPRING_PROFILES_ACTIVE` | `default` | Spring profile. |
| `JAVA_OPTS` | - | Options passed to `java`. |

## Build and test

To build the jar, run the tests in the image build stage, and smoke-test the image
(needs Docker with Buildx and `jq`):

```bash
docker buildx build --load -t sample-spring:dev .
scripts/smoke.sh sample-spring:dev
```

Without Docker, use JDK 27 and Maven 3.10.0 or later. `maven-enforcer-plugin` fails the build on older versions.

```bash
mvn -B verify
java -jar target/sample-spring-*.jar
```

| Component | Version | Where |
| --- | --- | --- |
| Spring Boot (parent and managed dependencies) | 4.1.2 | `pom.xml` |
| Java | 27 | `pom.xml` `java.version`, `eclipse-temurin:27-jre-alpine` |
| Maven | 3.10.0 | `maven:3.10.0-eclipse-temurin-27` in the `Dockerfile` |

## Health response

`GET /health` follows the health contract shared by sample-node, sample-spring and
sample-tomcat. sample-node's `/spring` and `/tomcat` paths pass this body through
unchanged, so callers can rely on the same fields.

| Field | Type | Meaning |
| --- | --- | --- |
| `result` | string | lowercase `ok` or `error`. This app returns `ok` while it can answer. |
| `version` | string | the `version` property (`VERSION` env, default `v0.0.0`) |
| `service` | string | always `sample-spring`: the name of the service that answered. sample-node's `/spring` compares this value with `sample-spring` to confirm that the right service answered. |
| `uptime` | integer | whole seconds since the JVM process started (JVM uptime in ms / 1000), `0` or more. It does not count from the request or from controller creation. |
| `type` | string | kept from the existing response: always `health` |

Other fields, such as `artifactId` from the jar manifest, may also appear. Example:

```bash
$ curl -s localhost:8080/health
{"result":"ok","service":"sample-spring","artifactId":"sample-spring","type":"health","version":"v0.0.0","uptime":4}
```

Only `/health` uses this contract. `/read`, `/loop/{count}` and the other endpoints
still return `"result":"OK"`, and `/actuator/health` returns Spring Boot Actuator's
`{"status":"UP"}`. The probe endpoints below use their own contract.

## Probe endpoints

`/live`, `/ready` and `/version` follow the probe contract shared by sample-node,
sample-spring and sample-tomcat, so the same Kubernetes probes and smoke checks work
for all three.

| Endpoint | Response |
| --- | --- |
| `GET /live` | HTTP 200 `{"live":true}` while the process answers, also during shutdown. |
| `GET /ready` | HTTP 200 `{"ready":true}` while Spring Boot's readiness state is `ACCEPTING_TRAFFIC`. HTTP 503 `{"ready":false}` otherwise. When shutdown starts (SIGTERM closes the context), Spring Boot sets the state to `REFUSING_TRAFFIC` before it stops the web server, so the load balancer stops routing here while in-flight requests finish. |
| `GET /version` | HTTP 200 `{"version":"v0.0.0","java":"27.0.1"}`. `version` is the `version` property (`VERSION` env, default `v0.0.0`); `java` is the JVM's `java.version`. |
| `GET /read` | unchanged: HTTP 200 `{"result":"OK","type":"live"}` |
| `GET /actuator/health` | Spring Boot Actuator: HTTP 200 `{"status":"UP"}` |

`scripts/smoke.sh` checks `/live`, `/ready`, `/version` and `/health` with `jq`
(for `/health`: `result == "ok"`, `version` and `service == "sample-spring"`), and
checks `/read` and `/actuator/health` for HTTP 200. It does not check the 503 from
`/ready` after SIGTERM, because a stopping container refuses new connections. The unit
tests check it instead: `ReadinessRefusedTests` publishes `REFUSING_TRAFFIC`, and
`ShutdownReadinessTests` closes a real servlet web server context.

The app does not need a database. The JPA and PostgreSQL dependencies in `pom.xml` are
commented out, and the repository has no Docker Compose file for them.

## Java agents

The image contains three Java agents. `entrypoint.sh` adds an agent to `JAVA_OPTS` only
when its variable is `true`. You can enable more than one agent. Other `JAVA_OPTS`
values are passed to `java`.

| Variable | Agent | Version | Source |
| --- | --- | --- | --- |
| `JAVA_AGENT_ENABLED` | Prometheus JMX Exporter, metrics on `:8081/metrics` (`jmx/config.yaml`) | 1.7.0 | [GitHub release](https://github.com/prometheus/jmx_exporter/releases/tag/1.7.0) |
| `DD_AGENT_ENABLED` | Datadog Java tracer (`dd-java-agent`) | 1.68.0 | [Maven Central](https://central.sonatype.com/artifact/com.datadoghq/dd-java-agent) |
| `CG_AGENT_ENABLED` | Amazon CodeGuru Profiler agent (standalone) | 1.2.7 | [Maven Central](https://central.sonatype.com/artifact/software.amazon.codeguruprofiler/codeguru-profiler-java-agent-standalone) |

The Dockerfile downloads each jar with `ADD --checksum=sha256:...`. The build fails if a
download does not match. To update an agent, change the URL and the SHA-256 published
with the release (`.sha256` file on Maven Central or the GitHub release asset digest).

Set `JAVA_AGENT_ENABLED=true` and expose port 8081 to scrape the JMX metrics. The other agents
need their own configuration to send data:

- Datadog: a reachable Datadog Agent (`DD_AGENT_HOST`, default `localhost:8126`) and
  the usual `DD_*` settings.
- CodeGuru: `AWS_CODEGURU_PROFILER_GROUP_NAME` (required, otherwise the profiler does
  not start), `AWS_CODEGURU_PROFILER_TARGET_REGION` and AWS credentials.

`scripts/smoke.sh` starts the image with no agent, with each agent, and with all three
agents and a `JAVA_OPTS` value. It checks `:8081/metrics` (JMX Exporter version, or no
answer when the JMX agent is off) and the Datadog and CodeGuru startup logs. It does not
configure a Datadog Agent or AWS credentials, so it does not check that data reaches
Datadog or CodeGuru.

## Web pages

`/` shows the host, date, cluster, profile, message and version, and two thumbs
buttons. `/drop/{rate}` (and `/drop` with a random rate) calls `/success/{rate}`
every 100 ms. It draws one canvas drop per response, with one color per version and
crimson for errors. A stacked progress bar shows the share of each version or error.

The templates load these CDN assets with Subresource Integrity (SRI) hashes:

| Asset | Version | Migration notes |
|---|---|---|
| Bootstrap CSS (jsDelivr) | 5.3.8 | [Bootstrap 5.3 migration](https://getbootstrap.com/docs/5.3/migration/) |
| jQuery (code.jquery.com) | 4.0.0 | [jQuery 4.0 upgrade guide](https://jquery.com/upgrade-guide/4.0/) |
| Font Awesome Free CSS (jsDelivr) | 7.3.1 | [Font Awesome upgrade](https://docs.fontawesome.com/upgrade/upgrade-on-web) |

The pages use no Bootstrap JavaScript components, so they do not load Bootstrap JS.

### Counter buttons need an external API

`static/js/counter.js` sends `GET` (every second), `POST` (thumbs up) and `DELETE`
(thumbs down) to `/counter/{up|down}` on the sample-node host. It finds that host by
replacing `sample-<name>` in the page host with `sample-node`. **The current
sample-node does not provide `/counter`.** The counts therefore stay at `0` and the
browser logs failed requests. This repository does not include a counter backend.

### Browser check

`src/test/browser/check.mjs` checks the templates and scripts in headless Chrome at
1280x800 and 375x812. It needs Node.js 22 or later, Chrome or Chromium, and network
access to the CDNs. It has no npm dependencies and is not part of `mvn verify` or CI.

```bash
CHROME_BIN=/path/to/chrome node src/test/browser/check.mjs
```

A local fixture server serves the real templates and `static/` files. It fakes
`/counter/{name}` and `/success/{rate}` (`100` returns version `v-fixture`; other rates
return HTTP 500), so the check never calls sample-node. Every line must print `ok`,
and the exit code is non-zero on failure. The check covers:

- jQuery 4.0.0, Bootstrap 5 button colors, Font Awesome 7 icons, header padding and
  no horizontal overflow.
- Counters: the initial `GET` fills both counts, one click sends exactly one `POST` or
  `DELETE`, the response updates the count, and only one 1-second poll runs.
- `/drop/100` and `/drop/0`: the canvas fills the viewport, and the stacked progress
  bar adds up to 100% with version colors or crimson for errors.

The fixture shows that the page code works with controlled responses. It does not
show that a real counter API exists or works. `EndpointTests` (`mvn verify`) checks
that `/` and `/drop/{rate}` render as HTML. To see the Thymeleaf values, open `/` on
the running jar.

## CI/CD

Pull requests and pushes to `main` run CI. A `v*` tag releases the image `ghcr.io/nalbam/sample-spring:<tag>`
and a GitHub Release with the jar. Jenkins can run the same pipeline with Nexus and Harbor or ECR.
See [docs/pipeline.md](docs/pipeline.md).
