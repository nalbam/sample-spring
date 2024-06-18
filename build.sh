#!/bin/bash

mkdir -p target

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
