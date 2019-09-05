#! /bin/bash

set -e

. "./set-env-${database}.sh"

./gradlew testClasses

./gradlew "${database}AllComposeDown"

./gradlew "${target}InfrastructureComposeBuild"

./gradlew "${target}InfrastructureComposeUp"

"./wait-for-${database}.sh"

./gradlew "${database}AllComposeUp"

if [[ "${SPRING_PROFILES_ACTIVE}" != "ActiveMQ" ]]; then
    ./gradlew :orders-and-customers-spring:cleanTest :orders-and-customers-micronaut:cleanTest build
else
    ./gradlew -x :orders-and-customers-micronaut:test -x :eventuate-tram-sagas-common-micronaut:test :orders-and-customers-spring:cleanTest build
fi

./gradlew "${database}AllComposeDown"
