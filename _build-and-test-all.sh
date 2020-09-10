#! /bin/bash

set -e

./gradlew testClasses

./gradlew "${database}AllComposeDown"

./gradlew "${target}InfrastructureComposeBuild"

./gradlew "${target}InfrastructureComposeUp"

./gradlew "${database}AllComposeUp"

if [[ "${SPRING_PROFILES_ACTIVE}" != "ActiveMQ" ]]; then
    ./gradlew :orders-and-customers-spring:cleanTest :orders-and-customers-micronaut:cleanTest build
else
    ./gradlew -x orders-and-customers-micronaut-integration-tests:test -x :orders-and-customers-micronaut:test -x :eventuate-tram-sagas-micronaut-common:test :orders-and-customers-spring:cleanTest build
fi

./gradlew "${database}AllComposeDown"
