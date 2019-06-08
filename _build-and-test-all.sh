#! /bin/bash

set -e

. "./set-env-${database}.sh"

./gradlew testClasses

./gradlew "${database}AllComposeDown"

./gradlew "${target}InfrastructureComposeBuild"

./gradlew "${target}InfrastructureComposeUp"

"./wait-for-${database}.sh"

./gradlew "${database}AllComposeUp"

./gradlew :orders-and-customers:cleanTest build

./gradlew "${database}AllComposeDown"
