#!/bin/bash

# Builds with the Maven version pinned in .mvn/wrapper/maven-wrapper.properties.

mkdir -p target

if [ -f ./target/VERSION ]; then
  VERSION=$(cat ./target/VERSION | xargs)
elif [ -f ./VERSION ]; then
  VERSION=$(cat ./VERSION | xargs)
fi

if [ "${VERSION}" == "" ]; then
  ./mvnw -B verify
else
  ./mvnw -B verify -Dthis.version=${VERSION}
fi
