#!/bin/bash

CMD=$1

USERNAME=${CIRCLE_PROJECT_USERNAME:-nalbam}
REPONAME=${CIRCLE_PROJECT_REPONAME:-sample-spring}

PORT=8080

command -v tput > /dev/null && TPUT=true

_echo() {
    if [ "${TPUT}" != "" ] && [ "$2" != "" ]; then
        echo -e "$(tput setaf $2)$1$(tput sgr0)"
    else
        echo -e "$1"
    fi
}

_result() {
    echo
    _echo "# $@" 4
}

_command() {
    echo
    _echo "$ $@" 3
}

_success() {
    echo
    _echo "+ $@" 2
    exit 0
}

_error() {
    echo
    _echo "- $@" 1
    exit 1
}

get_version() {
    if [ -f ./target/VERSION ]; then
        VERSION=$(cat ./target/VERSION | xargs)
    elif [ -f ./VERSION ]; then
        VERSION=$(cat ./VERSION | xargs)
    fi

    _result "VERSION=${VERSION}"
}

mvn_clean() {
    _command "mvn clean"
    mvn clean
}

mvn_build() {
    get_version

    if [ "${VERSION}" == "" ]; then
        _command "mvn package"
        mvn package
    else
        _command "mvn package -Dthis.version=${VERSION}"
        mvn package -Dthis.version=${VERSION}
    fi
}

docker_ps() {
    _command "docker ps -a"
    docker ps -a
}

docker_build() {
    _command "docker build -t ${USERNAME}/${REPONAME}:local ."
    docker build -t ${USERNAME}/${REPONAME}:local .
}

docker_run() {
    _command "docker run --name ${REPONAME} -p ${PORT}:${PORT} -d ${USERNAME}/${REPONAME}:local"
    docker run --name ${REPONAME} -p ${PORT}:${PORT} -d ${USERNAME}/${REPONAME}:local

    docker_ps

    _result "http://localhost:${PORT}"
}

docker_stop() {
    docker_ps

    CNT="$(docker ps -a | grep ${REPONAME} | wc -l | xargs)"
    if [ "x${CNT}" != "x0" ]; then
        _command "docker stop ${REPONAME}"
        docker stop ${REPONAME}

        _command "docker rm ${REPONAME}"
        docker rm ${REPONAME}
    fi

    docker_ps
}

_build() {
    case ${CMD} in
        start)
            mvn_clean
            mvn_build
            docker_build
            docker_run
            ;;
        stop)
            mvn_clean
            docker_stop
            ;;
        *)
            mvn_build
    esac
}

_build

_success
