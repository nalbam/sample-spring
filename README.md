# sample-spring

[![build](https://img.shields.io/github/actions/workflow/status/nalbam/sample-spring/push.yml?branch=main&style=for-the-badge&logo=github)](https://github.com/nalbam/sample-spring/actions/workflows/push.yml)
[![release](https://img.shields.io/github/v/release/nalbam/sample-spring?style=for-the-badge&logo=github)](https://github.com/nalbam/sample-spring/releases)

<!-- [![CircleCI](https://circleci.com/gh/nalbam/sample-spring.svg?style=svg)](https://circleci.com/gh/nalbam/sample-spring) -->

[![Docker Image Version (latest by date)](https://img.shields.io/docker/v/nalbam/sample-spring?label=Docker%20Hub&style=for-the-badge&logo=docker)](https://hub.docker.com/r/nalbam/sample-spring)
[![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/nalbam/sample-spring?style=for-the-badge&logo=docker)](https://hub.docker.com/r/nalbam/sample-spring)
[![Docker Pulls](https://img.shields.io/docker/pulls/nalbam/sample-spring?style=for-the-badge&logo=docker)](https://hub.docker.com/r/nalbam/sample-spring)

<!-- [![DockerHub Badge](http://dockeri.co/image/nalbam/sample-spring)](https://hub.docker.com/r/nalbam/sample-spring/) -->

## Docker

```bash
docker pull nalbam/sample-spring
```

## Build and run

Requirements: JDK 27. `./mvnw` downloads Maven 3.10.0 (version and SHA-256 are pinned in
`.mvn/wrapper/maven-wrapper.properties`). The build fails on an older Maven or JDK
(`maven-enforcer-plugin`), so use `./mvnw` or `./build.sh` instead of a system `mvn`.

| Component | Version | Where |
| --- | --- | --- |
| Spring Boot (parent and managed dependencies) | 4.1.2 | `pom.xml` |
| Java | 27 | `pom.xml` `java.version`, CI `setup-java` (Temurin), `eclipse-temurin:27-jre-alpine`, Jenkins `maven:3.10.0-eclipse-temurin-27-alpine` |
| Maven | 3.10.0 | `.mvn/wrapper/maven-wrapper.properties` |

```bash
# build and test (./mvnw -B verify), jar is created in target/
./build.sh

# run; spring.profiles.active is required
SPRING_PROFILES_ACTIVE=default java -jar target/sample-spring-*.jar

for path in / /health /live /actuator/health /actuator/prometheus; do
  curl -fsS -o /dev/null -w "%{http_code} ${path}\n" "localhost:8080${path}"
done
```

The app does not need a database. The JPA and PostgreSQL dependencies in `pom.xml` are
commented out, and the repository has no Docker Compose file for them.

Docker image (build the jar first; the Dockerfile copies `target/*.jar`):

```bash
docker build -t sample-spring .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=default sample-spring
```

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

The Helm chart sets `JAVA_AGENT_ENABLED=true` and exposes port 8081. The other agents
need their own configuration to send data:

- Datadog: a reachable Datadog Agent (`DD_AGENT_HOST`, default `localhost:8126`) and
  the usual `DD_*` settings.
- CodeGuru: `AWS_CODEGURU_PROFILER_GROUP_NAME` (required, otherwise the profiler does
  not start), `AWS_CODEGURU_PROFILER_TARGET_REGION` and AWS credentials.

Check agent startup with an image:

```bash
docker build -t sample-spring:ci .
./scripts/smoke-agents.sh sample-spring:ci
```

The script uses ports 8080 and 8081. It starts the container with no agent, with each
agent, and with all three agents and a `JAVA_OPTS` value. For each case it checks the
app endpoints above, `:8081/metrics` (JMX Exporter version and `jvm_` metrics, or no
answer when the JMX agent is off), and the Datadog and CodeGuru startup logs. It does
not configure a Datadog Agent or AWS credentials, so it does not check that data
reaches Datadog or CodeGuru.

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

## CI

`.github/workflows/push.yml` (check name `build`) runs on pull requests and pushes to
`main`/`master`, and is reused by the release workflow (`workflow_call`). On Temurin 27
it runs `./build.sh` (`./mvnw -B verify` with tests), starts the jar and checks `/`,
`/health`, `/live`, `/actuator/health` and `/actuator/prometheus`, builds the Docker
image from `target/*.jar`, checks that the container answers `/health` with
`SPRING_PROFILES_ACTIVE=default`, and runs `scripts/smoke-agents.sh`. It only reads the
repository: it does not bump versions, create releases, push images or dispatch
deployments.

## Release

Branch pushes only run CI. To release, push a `v*` SemVer tag such as `v0.10.1` or
`v0.11.0-rc.1`:

```bash
git tag v0.10.1
git push origin v0.10.1
```

Tags must match `vMAJOR.MINOR.PATCH` with an optional prerelease suffix, without
leading zeros or `+build` metadata, and be at most 128 characters (Docker's tag limit).
Other `v*` tags fail before CI runs.

The [release workflow](.github/workflows/release.yml) runs the CI workflow above, then:

1. Builds and pushes `nalbam/sample-spring:<tag>` and `ghcr.io/nalbam/sample-spring:<tag>`
   for `linux/amd64` and `linux/arm64`. Stable tags also update `latest`; prereleases
   only publish their version tag.
2. Creates a GitHub Release with commit notes since the previous reachable version
   tag, excluding merge commits and `chore: release` commits. Reruns update the
   existing release. Tags with a prerelease suffix create GitHub prereleases.
3. Sends a GitOps dispatch (`event_type=gitops`) to `opspresso/argocd-env-demo` for the
   `sample-spring` project's `app` container in `alpha` (`type=helm`).
4. For stable tags, sends the same version to `prod` through the GitHub `prod`
   environment, with `auto_merge=true` so GitOps updates its main branch directly.
   Prereleases stop at alpha.

Each stage requires the previous stage to succeed. Release runs are serialized across
tags because they share `latest` and the deployment targets. With `queue: max`, up to
100 releases wait in the queue without being cancelled; GitHub cancels any release
that arrives while 100 are already waiting. GitOps dispatches only request a
deployment: their success, and the prod job starting after alpha, do not confirm that
any rollout has completed.
Check Argo CD for the actual rollout.

### Artifacts

The image contains the jar that passed `mvn verify` in the same run. The CI job uploads
`target/*.jar` as the Actions artifact `jar-<commit SHA>` (tag runs only, missing jar
fails the job, kept 7 days). The image job checks out the same tag commit, downloads
that artifact into `target/` and builds with `context: .`; the Dockerfile copies it to
`/data/ROOT.jar`. The artifact only passes the jar between jobs: it is not deployed to a
Maven repository or attached to the GitHub Release.

### Check a release

After you push a tag, check the result:

1. In the *release* run (Actions), every job is green, and the `verify` job shows the
   `Check jar responds` and `Check Java agents` steps passed.
2. The run's *Artifacts* list has `jar-<commit SHA>` for the tag commit.
3. Both images exist for both platforms:

   ```bash
   docker buildx imagetools inspect nalbam/sample-spring:<tag>
   docker buildx imagetools inspect ghcr.io/nalbam/sample-spring:<tag>
   ```

4. Optional: run `./scripts/smoke-agents.sh nalbam/sample-spring:<tag>` against the
   published image.
5. The GitHub Release for the tag exists, and Argo CD shows the rollout.

### Versions

The Git tag is the release version: it is the image tag and the GitOps `version`.
It does not change the Maven version (`v0.10.0` in `pom.xml`, so the jar is always
`sample-spring-v0.10.0.jar`) or the version the app reports. `build.sh` passes
`-Dthis.version` from the `VERSION` file, but `pom.xml` does not use it. The app reports
the `VERSION` environment variable (default `v0.0.0`), which comes from the
deployment values in `opspresso/argocd-env-demo`, not from this workflow.
The `VERSION` and `BUMP` files are not used or updated by the release workflow.

### Repository settings

- Secrets: `DOCKER_USERNAME`/`DOCKER_PASSWORD` that can push to `nalbam/sample-spring`
  on Docker Hub; `GHP_TOKEN` that can send repository dispatches to
  `opspresso/argocd-env-demo`. GHCR uses the workflow's `GITHUB_TOKEN`.
- `GITHUB_TOKEN` permissions: `contents: read` by default, `packages: write` only in
  the image job, `contents: write` only in the GitHub Release job. The
  `ghcr.io/nalbam/sample-spring` package must allow this repository to write
  (package settings, *Manage Actions access*).
- Environment `prod` (Settings, Environments): add *Required reviewers* to approve each
  production dispatch; without protection rules the prod job runs right after alpha.
  You can also limit *Deployment branches and tags* to `v*` tags.
