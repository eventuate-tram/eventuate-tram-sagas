#! /bin/bash

set -e

export SPRING_PROFILES_ACTIVE=ActiveMQ
export database=mysql
export target=activemq
export broker=activemq

./_build-and-test-all.sh