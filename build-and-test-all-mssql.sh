#! /bin/bash

set -e

export SPRING_PROFILES_ACTIVE=mssql
export MICRONAUT_ENVIRONMENTS=mssql
export database=mssql
export target=mssql

./_build-and-test-all.sh