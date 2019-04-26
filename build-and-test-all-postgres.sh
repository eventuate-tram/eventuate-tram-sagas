#! /bin/bash

set -e

. ./set-env-postgres.sh

./gradlew testClasses

./gradlew postgresAllComposeDown

./gradlew postgresInfrastructureComposeBuild

./gradlew postgresInfrastructureComposeUp

./wait-for-postgres.sh

./gradlew postgresAllComposeUp

./gradlew :orders-and-customers:cleanTest build

./gradlew postgresAllComposeDown
