#! /bin/bash

set -e

. ./set-env-mysql.sh

./gradlew testClasses

./gradlew mysqlAllComposeDown

./gradlew mysqlInfrastructureComposeBuild

./gradlew mysqlInfrastructureComposeUp

./wait-for-mysql.sh

./gradlew mysqlAllComposeUp

./gradlew :orders-and-customers:cleanTest build

./gradlew mysqlAllComposeDown
