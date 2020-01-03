#!/bin/bash

if [ -f ./target/VERSION ]; then
    VERSION=$(cat ./target/VERSION | xargs)
elif [ -f ./VERSION ]; then
    VERSION=$(cat ./VERSION | xargs)
fi

echo "VERSION=${VERSION}"

if [ "${1}" == "docker" ] || [ "${1}" == "run" ]; then
    echo "$ mvn clean"
    mvn clean
fi

echo "$ mvn package -Dthis.version=${VERSION}"
mvn package -Dthis.version=${VERSION}

if [ "${1}" == "docker" ] || [ "${1}" == "run" ]; then
    echo "$ docker build -t nalbam/sample-spring:local ."
    docker build -t nalbam/sample-spring:local .
fi

if [ "${1}" == "stop" ] || [ "${1}" == "run" ]; then
    echo "$ docker ps -a"
    docker ps -a

    CNT="$(docker ps -a | grep 'sample-spring' | wc -l | xargs)"
    if [ "x${CNT}" != "x0" ]; then
        docker stop sample-spring
        docker rm sample-spring
    fi
fi

if [ "${1}" == "run" ]; then
    echo "$ docker run --name sample-spring -p 8080:8080 -d nalbam/sample-spring:local"
    docker run --name sample-spring -p 8080:8080 -d nalbam/sample-spring:local

    echo "$ docker ps -a"
    docker ps -a

    echo "# http://localhost:8080"
fi
