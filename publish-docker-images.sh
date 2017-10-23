#! /bin/bash -e

VERSION=${1?}

docker tag test-eventuate-tram-sagas-mysql eventuateio/eventuate-tram-sagas-mysql:${VERSION?}

docker push eventuateio/eventuate-tram-sagas-mysql:${VERSION?}
