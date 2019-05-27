#! /bin/bash

set -e

. ./set-env-mysql.sh

export database=mysql
export target=mysql

./_build-and-test-all.sh