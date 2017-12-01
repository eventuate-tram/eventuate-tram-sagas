#! /bin/bash -e


DOCKER_REPO=eventuateio
REMOTE_PREFIX=eventuate-tram-sagas
IMAGES="mysql"

DOCKER_COMPOSE_PREFIX=$(echo ${PWD##*/} | sed -e 's/-//g')_

BRANCH=$(git rev-parse --abbrev-ref HEAD)

if ! [[  $BRANCH =~ ^[0-9]+ ]] ; then
  echo Not release $BRANCH
  exit 0
fi

VERSION=$BRANCH

$PREFIX ./gradlew -P version=${VERSION} \
  -P deployUrl=https://dl.bintray.com/eventuateio-oss/eventuate-maven-release \
  testClasses bintrayUpload

function tagAndPush() {
  LOCAL=$1
  REMOTE="$REMOTE_PREFIX-$2"
  $PREFIX docker tag ${DOCKER_COMPOSE_PREFIX?}$LOCAL $DOCKER_REPO/$REMOTE:$VERSION
  $PREFIX docker tag ${DOCKER_COMPOSE_PREFIX?}$LOCAL $DOCKER_REPO/$REMOTE:latest
  echo Pushing $DOCKER_REPO/$REMOTE:$VERSION
  $PREFIX docker push $DOCKER_REPO/$REMOTE:$VERSION
  $PREFIX docker push $DOCKER_REPO/$REMOTE:latest
}

$PREFIX docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}

for image in $IMAGES ; do
    tagAndPush $(echo $image | sed -e 's/-//g')  $image
done
