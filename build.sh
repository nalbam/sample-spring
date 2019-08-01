#!/bin/bash

OS_NAME="$(uname | awk '{print tolower($0)}')"

SHELL_DIR=$(dirname $0)

RUN_PATH="."

if [ ! -f ${RUN_PATH}/target/VERSION ]; then
    return 1
fi

VERSION=$(cat ${RUN_PATH}/target/VERSION | xargs)

echo "VERSION=${VERSION}"

if [ "${VERSION}" == "" ]; then
    return 1
fi

mvn package -Dthis.version=${VERSION}
