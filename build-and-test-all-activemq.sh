#! /bin/bash

set -e

. ./set-env-mysql.sh

export SPRING_PROFILES_ACTIVE=ActiveMQ

./gradlew testClasses

./gradlew mysqlAllComposeDown

./gradlew activemqInfrastructureComposeBuild

./gradlew activemqInfrastructureComposeUp

./wait-for-mysql.sh

./gradlew mysqlAllComposeUp

./gradlew :orders-and-customers:cleanTest build

./gradlew mysqlAllComposeDown
