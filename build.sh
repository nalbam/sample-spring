#!/bin/bash

OS_NAME="$(uname | awk '{print tolower($0)}')"

SHELL_DIR=$(dirname $0)

RUN_PATH="."

if [ ! -f ${RUN_PATH}/target/VERSION ]; then
    return
fi

VERSION=$(cat ${RUN_PATH}/target/VERSION | xargs)
_result "VERSION=${VERSION}"

mvn package -Dthis.version=${VERSION}
