#! /bin/bash

set -e

./gradlew testClasses

export broker="${broker:-kafka}"
./gradlew "${database}${broker}AllComposeDown"
./gradlew "${database}${broker}AllComposeUp"


if [[ "${SPRING_PROFILES_ACTIVE}" != "ActiveMQ" ]]; then
    # ./gradlew :orders-and-customers-spring:cleanTest :orders-and-customers-micronaut:cleanTest build
    ./gradlew -x :orders-and-customers-spring-reactive:test build

    if [ "${database}" == "mysql" ]; then
      ./gradlew "${database}${broker}AllComposeDown"
      ./gradlew "${database}${broker}AllComposeUp"
      cat schema-for-testing-reactive-framework.sql | ./mysql-cli.sh -i
      ./gradlew :orders-and-customers-spring-reactive:test
    fi

    export EVENTUATE_OUTBOX_ID=1
    export USE_DB_ID=true

    ./gradlew "${database}${broker}AllComposeDown"
    ./gradlew "${database}${broker}AllComposeUp"

    ./gradlew :orders-and-customers-spring:cleanTest -x :orders-and-customers-spring-reactive:test build
 else
  # ./gradlew -x orders-and-customers-micronaut-integration-tests:test -x :orders-and-customers-micronaut:test -x :eventuate-tram-sagas-micronaut-common:test :orders-and-customers-spring:cleanTest build
  ./gradlew :orders-and-customers-spring:cleanTest -x :orders-and-customers-spring-reactive:test build
fi

./gradlew "${database}${broker}AllComposeDown"
