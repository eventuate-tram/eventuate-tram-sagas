#! /bin/bash

set -e

. ./set-env-postgres.sh

export database=postgres
export target=postgres

./_build-and-test-all.sh