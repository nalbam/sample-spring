#!/bin/bash

if [ -f ./target/VERSION ]; then
    VERSION=$(cat ./target/VERSION | xargs)
elif [ -f ./VERSION ]; then
    VERSION=$(cat ./VERSION | xargs)
fi

echo "VERSION=${VERSION}"

echo "$ mvn clean package"
mvn clean package -Dthis.version=${VERSION}

if [ "${1}" == "docker" ] || [ "${1}" == "run" ]; then
    docker build -t nalbam/sample-spring .
fi

if [ "${1}" == "run" ]; then
    echo "$ docker ps -a"
    docker ps -a

    CNT="$(docker ps -a | grep 'nalbam/sample-spring' | wc -l | xargs)"
    if [ "${CNT}" != "x0" ]; then
        docker stop sample-spring
        docker rm sample-spring
    fi

    echo "$ docker run --name sample-spring -p 8080:8080 -d nalbam/sample-spring"
    docker run --name sample-spring -p 8080:8080 -d nalbam/sample-spring

    echo "$ docker ps -a"
    docker ps -a

    echo "# http://localhost:8080"
fi
