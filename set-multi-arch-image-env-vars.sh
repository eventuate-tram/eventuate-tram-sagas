export MULTI_ARCH_TAG=test-build-${CIRCLE_SHA1?}
export BUILDX_PUSH_OPTIONS=--push

export MYSQL_MULTI_ARCH_IMAGE=eventuateio/eventuate-tram-sagas-mysql:$MULTI_ARCH_TAG
