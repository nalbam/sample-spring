#!/usr/bin/env bash
#
# Release steps shared by GitHub Actions (.github/workflows/release.yml) and the
# Jenkins library nalbam/jenkinsfiles. The same file is used in every sample-*
# repository. Run it from the repository root. See docs/pipeline.md.
#
# Usage:
#   scripts/release.sh tag TAG
#       Check a release tag. Prints prerelease=true|false and latest=true|false.
#       latest is true only for the highest stable tag, so an older tag never
#       moves the latest image back. Needs the repository tags in the checkout.
#   scripts/release.sh artifact NAME VERSION DIR
#       Export the Dockerfile "artifact" stage to DIR/NAME-VERSION.tar.gz
#       and DIR/NAME-VERSION.tar.gz.sha256.
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

# is_latest TAG prints true when TAG is the highest stable vX.Y.Z tag.
is_latest() {
  local tag="$1" highest
  git rev-parse -q --verify "refs/tags/$tag" > /dev/null ||
    die "tag $tag is not in this checkout; fetch the repository tags"
  highest="$(git tag -l 'v*' | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | sort -V | tail -n 1)"
  if [ "$tag" = "$highest" ]; then echo true; else echo false; fi
}

cmd_tag() {
  local prerelease latest
  prerelease="$(is_prerelease "$1")"
  latest="$(is_latest "$1")"
  echo "prerelease=$prerelease"
  echo "latest=$latest"
}

# curl_config NAME VALUE prints a curl config line. curl reads the secret from
# stdin, so it never appears on a command line.
curl_config() {
  local value="${2//\\/\\\\}"
  value="${value//\"/\\\"}"
  printf '%s = "%s"\n' "$1" "$value"
}

cmd_artifact() {
  local name="$1" version="$2" dir="$3"
  local file="$name-$version.tar.gz"
  mkdir -p "$dir"
  # Remove artifacts of earlier versions, so a reused workspace keeps one.
  rm -f "$dir/$name"-*.tar.gz "$dir/$name"-*.tar.gz.sha256
  # BuildKit writes the tar, so the result does not depend on the host tar.
  docker buildx build --target artifact --build-arg "VERSION=$version" \
    --output "type=tar,dest=-" . | gzip -n > "$dir/$file"
  [ "$(tar -tzf "$dir/$file" | grep -cv '/$')" -gt 0 ] || die "the artifact stage has no files"
  (cd "$dir" && sha256sum "$file" > "$file.sha256" && cat "$file.sha256")
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
  curl_config header "Authorization: Bearer $GITOPS_TOKEN" |
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
  deploy) [ $# -eq 2 ] || usage; cmd_deploy "$2" ;;
  *) usage ;;
esac
