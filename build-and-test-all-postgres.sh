#! /bin/bash

set -e

export SPRING_PROFILES_ACTIVE=postgres
export MICRONAUT_ENVIRONMENTS=postgres
export database=postgres
export target=postgres

./_build-and-test-all.sh