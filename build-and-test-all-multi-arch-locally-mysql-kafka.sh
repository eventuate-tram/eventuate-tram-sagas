#! /bin/bash -e

./gradlew $* testClasses --parallel

export EVENTUATE_COMMON_VERSION=$(sed < gradle.properties -e '/eventuateCommonImageVersion=/!d' -e 's/.*=//')

./mysql/build-docker-multi-arch.sh

docker pull localhost:5002/eventuate-tram-sagas-mysql:multi-arch-local-build

export database=mysql
export target=mysql

./build-and-test-all-mysql-kafka.sh $*

