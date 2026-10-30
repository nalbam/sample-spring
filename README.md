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

`.github/workflows/push.yml` runs on pull requests and on pushes to `main`/`master`.
Both run `./build.sh` (`mvn verify` with tests) and build the Docker image.
Version bump, GitHub release, GitOps dispatch and image push run only on push.
