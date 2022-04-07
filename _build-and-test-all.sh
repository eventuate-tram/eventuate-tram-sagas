#! /bin/bash

set -e

./gradlew testClasses

export broker="${broker:-kafka}"
./gradlew "${database}${broker}AllComposeDown"
./gradlew "${database}${broker}AllComposeUp"

if [ "${database}" == "mysql" ]; then
  cat schema-for-testing-reactive-framework.sql | ./mysql-cli.sh -i
fi

if [[ "${SPRING_PROFILES_ACTIVE}" != "ActiveMQ" ]]; then
    ./gradlew :orders-and-customers-spring:cleanTest :orders-and-customers-micronaut:cleanTest build

    export EVENTUATE_OUTBOX_ID=1
    export USE_DB_ID=true

    ./gradlew "${database}${broker}AllComposeDown"
    ./gradlew "${database}${broker}AllComposeUp"

    ./gradlew :orders-and-customers-spring:cleanTest :orders-and-customers-micronaut:cleanTest build
else
    ./gradlew -x orders-and-customers-micronaut-integration-tests:test -x :orders-and-customers-micronaut:test -x :eventuate-tram-sagas-micronaut-common:test :orders-and-customers-spring:cleanTest build
fi

./gradlew "${database}${broker}AllComposeDown"
