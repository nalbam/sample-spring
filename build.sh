#!/bin/bash

if [ -f ./target/VERSION ]; then
  VERSION=$(cat ./target/VERSION | xargs)
elif [ -f ./VERSION ]; then
  VERSION=$(cat ./VERSION | xargs)
fi

if [ "${VERSION}" == "" ]; then
  mvn package
else
  mvn package -Dthis.version=${VERSION}
fi

# jmx
if [ -f jmx/config.yaml ]; then
  mkdir -p target/jmx
  cp jmx/config.yaml target/jmx/config.yaml
  cp jmx/jmx_* target/jmx/jmx_javaagent.jar
fi

# entrypoint.sh
if [ -f ./entrypoint.sh ]; then
  cp ./entrypoint.sh target/entrypoint.sh
fi
