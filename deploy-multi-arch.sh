#! /bin/bash -e

BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [[  $BRANCH == "master" ]] ; then
  TARGET_TAG=$(sed -e '/^version=/!d' -e 's/version=//' -e 's/-SNAPSHOT/.BUILD-SNAPSHOT/' < gradle.properties)
elif [[  $BRANCH =~ RELEASE$ ]] ; then
  TARGET_TAG=$BRANCH
elif [[  $BRANCH =~ M[0-9]+$ ]] ; then
  TARGET_TAG=$BRANCH
elif [[  $BRANCH =~ RC[0-9]+$ ]] ; then
  TARGET_TAG=$BRANCH
else
  TARGET_TAG=${BRANCH}-${CIRCLE_BUILD_NUM?}
fi

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
