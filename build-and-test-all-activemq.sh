#! /bin/bash

set -e

. ./set-env-mysql.sh

export SPRING_PROFILES_ACTIVE=ActiveMQ
export database=mysql
export target=activemq

./_build-and-test-all.sh