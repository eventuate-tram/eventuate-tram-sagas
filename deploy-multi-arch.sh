#!/bin/bash
set -e

docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}

echo "Publishing multi-architecture Docker images with tag: ${MULTI_ARCH_TAG}"

./gradlew publishMultiArchContainerImages -P "multiArchTag=${MULTI_ARCH_TAG}"
