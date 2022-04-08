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
elif [[  $BRANCH =~ ^wip- ]] ; then
   TARGET_TAG=$(sed -e '/^version=/!d' -e 's/version=//' -e 's/-SNAPSHOT//' < gradle.properties)
   TARGET_TAG=${TARGET_TAG}.$(echo $BRANCH | sed -e 's/wip-//' -e 's/-/_/' | tr '[:lower:]' '[:upper:]' )
   TARGET_TAG=${TARGET_TAG}.BUILD-SNAPSHOT
else
  TARGET_TAG=${BRANCH}-${CIRCLE_BUILD_NUM?}
fi

echo $TARGET_TAG
