#!/bin/bash

CMD=$1

RUNNER=

USERNAME="nalbam"
REPONAME="$(basename ${PWD})"

REGISTRY="${REPONAME}"

if [ "${CIRCLE_PROJECT_USERNAME}" != "" ]; then
    # circle-ci
    RUNNER="circle-ci"

    USERNAME=${CIRCLE_PROJECT_USERNAME:-$USERNAME}
    REPONAME=${CIRCLE_PROJECT_REPONAME:-$REPONAME}

    REGISTRY="${USERNAME}/${REPONAME}"
fi

if [ "${GITLAB_USER_ID}" != "" ]; then
    # gitlab
    RUNNER="gitlab"

    USERNAME=${GITLAB_USER_ID:-$USERNAME}
    REPONAME=${CI_PROJECT_NAME:-$REPONAME}

    REGISTRY="${REPONAME}"
fi

# default port
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

npm_build() {
    _command "npm run build"
    npm run build
}

npm_start() {
    _command "npm run start"
    npm run start
}

npm_clean() {
    _command "npm run clean"
    npm run clean
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

    # jmx
    if [ -f jmx/config.yaml ]; then
        mkdir -p target/jmx
        cp jmx/config.yaml target/jmx/config.yaml
        cp jmx/jmx_* target/jmx/jmx_javaagent.jar
    fi
}

chart_build() {
    get_version

    pushd charts/${REPONAME}

    sed -i -e \"s/name: .*/name: ${REPONAME}/\" Chart.yaml
    sed -i -e \"s/appVersion: .*/appVersion: ${VERSION}/\" Chart.yaml
    sed -i -e \"s/version: .*/version: ${VERSION}/\" Chart.yaml
    sed -i -e \"s/tag: .*/tag: ${VERSION}/g\" values.yaml

    sed -i -e \"s|repository: .*|repository: ${REGISTRY}|\" values.yaml

    helm lint .

    helm push . chartmuseum

    popd
}

chart_push() {
    get_version

}

docker_ps() {
    _command "docker ps -a"
    docker ps -a
}

docker_build() {
    get_version

    _command "docker build -t ${REGISTRY}:${VERSION} ."
    docker build -t ${REGISTRY}:${VERSION} .
}

docker_push() {
    get_version

    _command "docker push ${REGISTRY}:${VERSION}"
    docker push ${REGISTRY}:${VERSION}
}

docker_run() {
    get_version

    _command "docker run --name ${REPONAME} -p ${PORT}:${PORT} -d ${REGISTRY}:${VERSION}"
    docker run --name ${REPONAME} -p ${PORT}:${PORT} -d ${REGISTRY}:${VERSION}

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

        docker_ps
    fi
}

_clean() {
    # npm
    if [ -f ./package.json ]; then
        npm_clean
    fi

    # mvn
    if [ -f ./pom.xml ]; then
        mvn_clean
    fi
}

_build() {
    mkdir -p target

    # npm
    if [ -f ./package.json ]; then
        npm_build
    fi

    # mvn
    if [ -f ./pom.xml ]; then
        mvn_build
    fi

    # entrypoint.sh
    if [ -f ./entrypoint.sh ]; then
        cp ./entrypoint.sh target/entrypoint.sh
    fi
}

_bump() {
    date > BUMP

    mkdir build

    echo "bump" > build/commit_message.txt
}

_run() {
    case ${CMD} in
        clean)
            _clean
            docker_stop
            ;;
        start)
            docker_stop
            docker_build
            docker_run
            ;;
        stop)
            docker_stop
            ;;
        docker)
            docker_build
            docker_push
            ;;
        chart)
            chart_build
            chart_push
            ;;
        bump)
            _bump
            ;;
        *)
            _build
    esac
}

_run

_success
