# Open pull request review

This document records whether each open pull request (PR) is still needed. A
maintainer can use it to close or keep each PR. This review does not merge, close or
change any PR.

## Review scope

| Item | Value |
| --- | --- |
| Queried at | 2026-11-01T07:43:07Z |
| Reviewed `main` | [`58184e3cf752b9f5abc2a8b88f1286ffa8910210`](https://github.com/nalbam/sample-spring/commit/58184e3cf752b9f5abc2a8b88f1286ffa8910210) |
| PR list source | `gh api --paginate 'repos/nalbam/sample-spring/pulls?state=open&per_page=100'` with an authenticated `gh` (the query succeeded; the list was not empty) |
| Build tools | Temurin JDK 27.0.1, Maven 3.10.0 from `.mvn/wrapper/maven-wrapper.properties` (`./mvnw`) |
| Maven commands | `./mvnw -B help:effective-pom` and `./mvnw -B dependency:tree` on the reviewed `main` |

When the query ran, the open PRs were #246, #249 and #250. All three come from
`dependabot[bot]`, change only `pom.xml`, and have the GitHub merge state
`CONFLICTING`. Their only checks are *CodeRabbit* (review skipped) and the Mergify
*Summary*. The `build` workflow did not run on them.

The PR that adds this document is a work PR from this review. It is not one of the
reviewed PRs.

## Result

### Applied

- **In this review: none.** This review changes only `docs/open-pr-review.md` and
  `README.md`. It does not change `pom.xml` or any dependency.
- **Already on `main`:** [#251](https://github.com/nalbam/sample-spring/pull/251)
  (merge commit `f33b7cf`) changed the parent from `3.2.5` to `4.1.2` and removed the
  direct `logback-classic` and `jackson-databind` declarations.
  [#253](https://github.com/nalbam/sample-spring/pull/253) (merge commit `14ab521`)
  moved the build to Java 27 and Maven 3.10.0. It did not change dependency versions.

### Can be closed

| PR | Proposed change | Current state on `main` | Decision | Replaced by |
| --- | --- | --- | --- | --- |
| [#249](https://github.com/nalbam/sample-spring/pull/249) | `org.springframework.boot:spring-boot-starter-parent` `3.2.5` → `3.3.3` | Parent `4.1.2` in `pom.xml` | Can be closed | [#251](https://github.com/nalbam/sample-spring/pull/251) |
| [#250](https://github.com/nalbam/sample-spring/pull/250) | `ch.qos.logback:logback-classic` `1.4.14` → `1.5.8` (explicit `test` scope) | No direct declaration; resolves to `1.5.38` (`compile`) through Spring Boot | Can be closed | [#251](https://github.com/nalbam/sample-spring/pull/251) |
| [#246](https://github.com/nalbam/sample-spring/pull/246) | `com.fasterxml.jackson.core:jackson-databind` `2.17.0` → `2.17.2` | No direct declaration; artifact not in the dependency tree. The app uses `tools.jackson.core:jackson-databind` `3.1.7` | Can be closed | [#251](https://github.com/nalbam/sample-spring/pull/251) |

Do not restore the old direct version pins from these PRs. Spring Boot manages the
versions now.

## Details

### #249: Spring Boot parent 3.3.3

- PR: <https://github.com/nalbam/sample-spring/pull/249>, head `176cfd0`.
- Proposed: `spring-boot-starter-parent` `3.3.3`.
- Current: `pom.xml` declares `spring-boot-starter-parent` `4.1.2`. #251 made this
  change.
- Reason: `main` already uses a newer Spring Boot line than the PR. Merging the PR
  would downgrade Spring Boot, and the PR conflicts with `main`.

### #250: logback-classic 1.5.8

- PR: <https://github.com/nalbam/sample-spring/pull/250>, head `adaa7a8`.
- Proposed: a direct `ch.qos.logback:logback-classic` `1.5.8` dependency with
  `<scope>test</scope>`.
- Current declaration: none. #251 removed the direct `logback-classic` `1.4.14`
  declaration from `pom.xml`.
- Management: the `spring-boot-dependencies` `4.1.2` BOM (imported by the parent) sets
  `logback.version=1.5.38`. The effective POM has
  `ch.qos.logback:logback-classic:1.5.38` in `dependencyManagement`.
- Resolved (`dependency:tree`):

  ```text
  +- org.springframework.boot:spring-boot-starter-actuator:jar:4.1.2:compile
  |  +- org.springframework.boot:spring-boot-starter:jar:4.1.2:compile
  |  |  +- org.springframework.boot:spring-boot-starter-logging:jar:4.1.2:compile
  |  |  |  +- ch.qos.logback:logback-classic:jar:1.5.38:compile
  |  |  |  |  \- ch.qos.logback:logback-core:jar:1.5.38:compile
  ```

- Reason: the resolved version `1.5.38` is newer than `1.5.8`. The app uses Logback at
  runtime (`src/main/resources/logback.xml`) through `spring-boot-starter-logging` in
  `compile` scope, so it does not need the PR's direct `test` dependency.

### #246: jackson-databind 2.17.2

- PR: <https://github.com/nalbam/sample-spring/pull/246>, head `d337845`.
- Proposed: a direct `com.fasterxml.jackson.core:jackson-databind` `2.17.2`
  dependency (Jackson 2).
- Current declaration: none. #251 removed the direct `jackson-databind` `2.17.0`
  declaration and moved
  `src/main/java/com/nalbam/sample/controller/RestfulController.java` to Jackson 3
  (`tools.jackson.databind.json.JsonMapper`, `tools.jackson.core.type.TypeReference`).
- Jackson 2 and Jackson 3 are different artifacts. Jackson 2 uses the group
  `com.fasterxml.jackson.core`. Jackson 3 uses the group `tools.jackson.core`. A
  version of one does not update the other.
- Management: the `spring-boot-dependencies` `4.1.2` BOM sets
  `jackson-bom.version=3.1.7` (Jackson 3) and `jackson-2-bom.version=2.21.7`
  (Jackson 2, for compatibility). The effective POM manages
  `tools.jackson.core:jackson-databind:3.1.7` and
  `com.fasterxml.jackson.core:jackson-databind:2.21.7`.
- Resolved (`dependency:tree`):

  ```text
  +- org.springframework.boot:spring-boot-starter-web:jar:4.1.2:compile
  |  +- org.springframework.boot:spring-boot-starter-jackson:jar:4.1.2:compile
  |  |  \- org.springframework.boot:spring-boot-jackson:jar:4.1.2:compile
  |  |     \- tools.jackson.core:jackson-databind:jar:3.1.7:compile
  |  |        +- com.fasterxml.jackson.core:jackson-annotations:jar:2.21:compile
  |  |        \- tools.jackson.core:jackson-core:jar:3.1.7:compile
  ```

  `./mvnw -B dependency:tree -Dincludes=com.fasterxml.jackson.core:jackson-databind`
  prints no dependency. Only `jackson-annotations` `2.21` comes from the
  `com.fasterxml.jackson.core` group; Jackson 3 `jackson-databind` uses it.
- Reason: the build does not use Jackson 2 `jackson-databind`. If a dependency needs it
  later, Spring Boot manages it at `2.21.7`, which is newer than `2.17.2`.

## Check again

Run these commands from the repository root to repeat this review. See also
[Dependency review](../README.md#dependency-review) in the README.

1. List the open PRs. `gh` must be authenticated (`gh auth status`):

   ```bash
   gh api --paginate 'repos/nalbam/sample-spring/pulls?state=open&per_page=100' \
     --jq '.[] | [.number, .user.login, .head.ref, .title] | @tsv'
   ```

   If the command fails (non-zero exit code, for example no authentication or rate
   limit), the list is unknown. Do not treat the failure as "no open PRs".

2. Read a PR's body, diff and checks:

   ```bash
   gh pr view 246
   gh pr diff 246
   gh pr checks 246
   ```

3. Check the managed versions and resolved coordinates with JDK 27 and `./mvnw`:

   ```bash
   ./mvnw -B help:effective-pom -Doutput=target/effective-pom.xml
   grep -nE '<(logback|jackson-bom|jackson-2-bom)\.version>' target/effective-pom.xml
   ./mvnw -B dependency:tree -Dincludes='ch.qos.logback,tools.jackson.core,com.fasterxml.jackson.core'
   ```

   Expected on the reviewed `main`: `logback.version` `1.5.38`, `jackson-bom.version`
   `3.1.7`, `jackson-2-bom.version` `2.21.7`, and the tree entries shown in
   [Details](#details).

## Build check

The `build` check (`.github/workflows/push.yml`) runs `./build.sh` (`./mvnw -B verify`
with `EndpointTests`), checks the jar endpoints, builds the Docker image, checks
`/health` in the container and runs `scripts/smoke-agents.sh`.

| Revision | Result |
| --- | --- |
| Reviewed `main` `58184e3` | `build` [run 41205120572](https://github.com/nalbam/sample-spring/actions/runs/41205120572): success (all steps passed; *Upload jar* is skipped on non-tag runs) |
| Reviewed `main` `58184e3`, local | `./mvnw -B verify` with JDK 27.0.1 and Maven 3.10.0: `BUILD SUCCESS`, 14 tests, 0 failures. Jar, Docker and agent checks were not run locally. |
| Work PR [#255](https://github.com/nalbam/sample-spring/pull/255) `d368721` (`d36872132f9fc3d49b75e9b9397db9906d9fd672`) | `build` [run 41286236556](https://github.com/nalbam/sample-spring/actions/runs/41286236556): success. *Run Build*, *Check jar responds*, *Build image*, *Check container responds on /health* and *Check Java agents* passed; *Upload jar* is skipped on non-tag runs. |

Commit `d368721` changes only `README.md` and this document on top of `58184e3`. Later
commits on the work PR that update this document get their own `build` run in the
PR checks.
