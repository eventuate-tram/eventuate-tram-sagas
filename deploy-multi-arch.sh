#! /bin/bash -e

TARGET_TAG=$(./.circleci/target-tag.sh)
docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}

retag() {
  BASE=$1
  IMAGE=${BASE}:${MULTI_ARCH_TAG?}
  TARGET_IMAGE=$BASE:$TARGET_TAG

  echo Retagging $IMAGE $TARGET_IMAGE

  SOURCES=$(docker manifest inspect $IMAGE | \
  jq -r '.manifests[].digest  | sub("^"; "'${BASE}'@")')

  docker buildx imagetools create -t ${TARGET_IMAGE} $SOURCES
}

retag "eventuateio/eventuate-tram-sagas-mysql"
