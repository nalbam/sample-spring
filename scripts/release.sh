#!/usr/bin/env bash
#
# Release steps shared by GitHub Actions (.github/workflows/release.yml) and
# Jenkins (Jenkinsfile). The same file is used in every sample-* repository.
# Run it from the repository root. See docs/pipeline.md.
#
# Usage:
#   scripts/release.sh tag TAG
#       Check a release tag. Prints prerelease=true or prerelease=false.
#   scripts/release.sh artifact NAME VERSION DIR
#       Export the Dockerfile "artifact" stage to DIR/NAME-VERSION.tar.gz
#       and DIR/NAME-VERSION.tar.gz.sha256.
#   scripts/release.sh nexus NAME VERSION DIR
#       Upload the artifact files in DIR to a Nexus raw repository.
#       Env: NEXUS_URL, NEXUS_USER, NEXUS_PASSWORD, NEXUS_REPOSITORY (default raw-releases).
#   scripts/release.sh image REGISTRY NAME VERSION
#       Log in to REGISTRY (Harbor or Amazon ECR), then build and push
#       REGISTRY/NAME:VERSION for PLATFORMS (default linux/amd64,linux/arm64).
#       Stable versions also push REGISTRY/NAME:latest.
#       Env: REGISTRY_USER and REGISTRY_PASSWORD for Harbor; AWS credentials for ECR.
#   scripts/release.sh deploy PHASE
#       Send a GitOps repository_dispatch event that deploys VERSION to PHASE.
#       Env: GITOPS_REPO, GITOPS_TOKEN, APP_NAME, VERSION,
#            IMAGE_OWNER (default nalbam), AUTO_MERGE (true or false, default false).

set -euo pipefail

usage() {
  sed -n '/^# Usage:/,/^$/p' "$0" | sed 's/^# \{0,1\}//' >&2
  exit 2
}

die() {
  echo "error: $*" >&2
  exit 1
}

# vMAJOR.MINOR.PATCH with an optional SemVer prerelease. No build metadata,
# because "+" is not valid in an image tag.
is_prerelease() {
  local tag="$1"
  local number='(0|[1-9][0-9]*)'
  local identifier='(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)'
  local pattern="^v${number}\.${number}\.${number}(-${identifier}(\.${identifier})*)?$"
  if [[ ! "$tag" =~ $pattern || ${#tag} -gt 128 ]]; then
    die "'$tag' is not vMAJOR.MINOR.PATCH[-PRERELEASE] (128 characters maximum)"
  fi
  if [[ "$tag" == *-* ]]; then echo true; else echo false; fi
}

cmd_tag() {
  local prerelease
  prerelease="$(is_prerelease "$1")"
  echo "prerelease=$prerelease"
}

cmd_artifact() {
  local name="$1" version="$2" dir="$3"
  local file="$name-$version.tar.gz"
  mkdir -p "$dir"
  rm -f "$dir/$file" "$dir/$file.sha256"
  # BuildKit writes the tar, so the result does not depend on the host tar.
  docker buildx build --target artifact --build-arg "VERSION=$version" \
    --output "type=tar,dest=-" . | gzip -n > "$dir/$file"
  [ "$(tar -tzf "$dir/$file" | grep -cv '/$')" -gt 0 ] || die "the artifact stage has no files"
  (cd "$dir" && sha256sum "$file" > "$file.sha256" && cat "$file.sha256")
}

cmd_nexus() {
  local name="$1" version="$2" dir="$3"
  : "${NEXUS_URL:?NEXUS_URL is required}"
  : "${NEXUS_USER:?NEXUS_USER is required}"
  : "${NEXUS_PASSWORD:?NEXUS_PASSWORD is required}"
  local base="${NEXUS_URL%/}/repository/${NEXUS_REPOSITORY:-raw-releases}/$name/$version"
  local file
  for file in "$dir/$name-$version.tar.gz" "$dir/$name-$version.tar.gz.sha256"; do
    [ -f "$file" ] || die "$file not found; run the artifact command first"
    # The password goes to curl on stdin, not on the command line.
    printf 'user = "%s:%s"\n' "$NEXUS_USER" "$NEXUS_PASSWORD" |
      curl -fsS --config - --upload-file "$file" "$base/$(basename "$file")"
    echo "uploaded $base/$(basename "$file")"
  done
}

cmd_image() {
  local registry="${1%/}" name="$2" version="$3"
  local image="$registry/$name"
  local host="${registry%%/*}"
  local prerelease
  prerelease="$(is_prerelease "$version")"

  if [[ "$host" =~ ^[0-9]{12}\.dkr\.ecr\.([a-z0-9-]+)\.amazonaws\.com$ ]]; then
    local region="${BASH_REMATCH[1]}"
    local repository="${image#"$host"/}"
    aws ecr get-login-password --region "$region" |
      docker login --username AWS --password-stdin "$host"
    if ! aws ecr describe-repositories --region "$region" --repository-names "$repository" > /dev/null 2>&1; then
      aws ecr create-repository --region "$region" --repository-name "$repository" \
        --image-scanning-configuration scanOnPush=true > /dev/null
      echo "created ECR repository $repository"
    fi
  else
    : "${REGISTRY_USER:?REGISTRY_USER is required for $host}"
    : "${REGISTRY_PASSWORD:?REGISTRY_PASSWORD is required for $host}"
    printf '%s' "$REGISTRY_PASSWORD" | docker login --username "$REGISTRY_USER" --password-stdin "$host"
  fi

  local tags=(--tag "$image:$version")
  if [ "$prerelease" = false ]; then
    tags+=(--tag "$image:latest")
  fi
  docker buildx build --platform "${PLATFORMS:-linux/amd64,linux/arm64}" --push \
    --build-arg "VERSION=$version" \
    --label "org.opencontainers.image.version=$version" \
    --label "org.opencontainers.image.revision=$(git rev-parse HEAD)" \
    "${tags[@]}" .
  docker buildx imagetools inspect "$image:$version"
}

cmd_deploy() {
  local phase="$1"
  : "${GITOPS_REPO:?GITOPS_REPO is required}"
  : "${GITOPS_TOKEN:?GITOPS_TOKEN is required}"
  : "${APP_NAME:?APP_NAME is required}"
  : "${VERSION:?VERSION is required}"
  local auto_merge="${AUTO_MERGE:-false}"
  [[ "$auto_merge" == true || "$auto_merge" == false ]] || die "AUTO_MERGE must be true or false"
  local payload
  payload="$(jq -n \
    --arg username "${IMAGE_OWNER:-nalbam}" \
    --arg project "$APP_NAME" \
    --arg version "$VERSION" \
    --arg phase "$phase" \
    --argjson auto_merge "$auto_merge" \
    '{event_type: "gitops", client_payload: {
      username: $username, project: $project, version: $version,
      container: "app", phase: $phase, type: "helm", auto_merge: $auto_merge}}')"
  jq -c '.client_payload' <<< "$payload"
  # The token goes to curl on stdin, not on the command line.
  printf 'header = "Authorization: Bearer %s"\n' "$GITOPS_TOKEN" |
    curl -fsS --config - -X POST \
      -H "Accept: application/vnd.github+json" \
      -H "X-GitHub-Api-Version: 2022-11-28" \
      --data "$payload" \
      "https://api.github.com/repos/$GITOPS_REPO/dispatches"
  echo "sent gitops event: $APP_NAME $VERSION to $GITOPS_REPO ($phase)"
}

case "${1:-}" in
  tag) [ $# -eq 2 ] || usage; cmd_tag "$2" ;;
  artifact) [ $# -eq 4 ] || usage; cmd_artifact "$2" "$3" "$4" ;;
  nexus) [ $# -eq 4 ] || usage; cmd_nexus "$2" "$3" "$4" ;;
  image) [ $# -eq 4 ] || usage; cmd_image "$2" "$3" "$4" ;;
  deploy) [ $# -eq 2 ] || usage; cmd_deploy "$2" ;;
  *) usage ;;
esac
