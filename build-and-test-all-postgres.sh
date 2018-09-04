#! /bin/bash

set -e

. ./set-env-postgres.sh

docker-compose -f docker-compose-postgres.yml down --remove-orphans -v

docker-compose -f docker-compose-postgres.yml up -d --build zookeeper postgres kafka

./wait-for-postgres.sh

docker-compose -f docker-compose-postgres.yml up -d --build

./gradlew :orders-and-customers:cleanTest build

docker-compose -f docker-compose-postgres.yml down --remove-orphans -v
