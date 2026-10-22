#! /bin/bash -e

docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}

export EVENTUATE_COMMON_DATABASE_VERSION=$(sed < gradle.properties -e '/eventuateCommonDatabaseImageVersion=/!d' -e 's/.*=//')

./mysql/build-docker-multi-arch.sh
