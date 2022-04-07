#! /bin/bash -e

docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}

export EVENTUATE_COMMON_VERSION=$(sed < gradle.properties -e '/eventuateCommonImageVersion=/!d' -e 's/.*=//')

./mysql/build-docker-multi-arch.sh
