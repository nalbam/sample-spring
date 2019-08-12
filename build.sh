#!/bin/bash

RUN_PATH="."

if [ -f ${RUN_PATH}/target/VERSION ]; then
    VERSION=$(cat ${RUN_PATH}/target/VERSION | xargs)

    echo "VERSION=${VERSION}"

    mvn package -Dthis.version=${VERSION}
fi
