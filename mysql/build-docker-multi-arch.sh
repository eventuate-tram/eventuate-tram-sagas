#! /bin/bash -e

SCRIPT_DIR=$(cd $( dirname "${BASH_SOURCE[0]}" ) ; pwd)

# docker compose -f $SCRIPT_DIR/../docker-compose-registry.yml --project-name eventuate-common-registry up -d registry

IMAGE="${MYSQL_MULTI_ARCH_IMAGE:-${DOCKER_HOST_NAME:-host.docker.internal}:5002/eventuate-tram-sagas-mysql:multi-arch-local-build}"
OPTS="${BUILDX_PUSH_OPTIONS:---output=type=image,push=true,registry.insecure=true}"

echo IMAGE=$IMAGE
echo OPTS=$OPTS

export BUILDKIT_INLINE_CACHE=1
export DOCKER_BUILDKIT=0
export REGISTRY_INSECURE=1
export BUILDKIT_NO_CLIENT_TOKEN=true

docker buildx build --platform linux/amd64,linux/arm64 \
  -t $IMAGE \
  -f $SCRIPT_DIR/Dockerfile \
  --build-arg=EVENTUATE_COMMON_VERSION=${EVENTUATE_COMMON_VERSION?} \
  $OPTS \
  $SCRIPT_DIR
