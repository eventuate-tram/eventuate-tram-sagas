#! /bin/bash -e


DOCKER_REPO=eventuateio
REMOTE_PREFIX=eventuate-tram-sagas
IMAGES="mysql postgres"

DOCKER_COMPOSE_PREFIX=$(echo ${PWD##*/} | sed -e 's/-//g')_

BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [  $BRANCH == "master" ] ; then
  BUILD_SNAPSHOT=$(sed -e '/^version=/!d' -e 's/^version=\(.*\)-SNAPSHOT$/\1.BUILD-SNAPSHOT/' < gradle.properties)
  echo master: publishing $BUILD_SNAPSHOT
  ./gradlew -P version=$BUILD_SNAPSHOT -P deployUrl=${S3_REPO_DEPLOY_URL?} uploadArchives
  exit 0
fi

if ! [[  $BRANCH =~ ^[0-9]+ ]] ; then
  echo Not release $BRANCH - no PUSH
  exit 0
elif [[  $BRANCH =~ RELEASE$ ]] ; then
  BINTRAY_REPO_TYPE=release
elif [[  $BRANCH =~ M[0-9]+$ ]] ; then
    BINTRAY_REPO_TYPE=milestone
elif [[  $BRANCH =~ RC[0-9]+$ ]] ; then
    BINTRAY_REPO_TYPE=rc
else
  echo cannot figure out bintray for this branch $BRANCH
  exit -1
fi

echo BINTRAY_REPO_TYPE=${BINTRAY_REPO_TYPE}

VERSION=$BRANCH

$PREFIX ./gradlew -P version=${VERSION} \
  -P bintrayRepoType=${BINTRAY_REPO_TYPE} \
  -P deployUrl=https://dl.bintray.com/eventuateio-oss/eventuate-maven-${BINTRAY_REPO_TYPE} \
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
