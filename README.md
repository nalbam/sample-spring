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

Requirements: JDK 25 and Maven 3.9 or later (CI uses Corretto 25).

```bash
# build and test (mvn verify), jar is created in target/
./build.sh

# run; spring.profiles.active is required
SPRING_PROFILES_ACTIVE=default java -jar target/sample-spring-*.jar

curl localhost:8080/health
```

Docker image (build the jar first; the Dockerfile copies `target/*.jar`):

```bash
docker build -t sample-spring .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=default sample-spring
```

## CI

`.github/workflows/push.yml` (check name `build`) runs on pull requests and pushes to
`main`/`master`, and is reused by the release workflow (`workflow_call`). It runs
`./build.sh` (`mvn -B verify` with tests) on Corretto 25, builds the Docker image from
`target/*.jar`, and checks that the container answers `/health` with
`SPRING_PROFILES_ACTIVE=default`. It only reads the repository: it does not bump
versions, create releases, push images or dispatch deployments.

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
