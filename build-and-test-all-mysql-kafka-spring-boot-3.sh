#! /bin/bash -e

export database=mysql
export target=mysql

./_build-and-test-all.sh -P springBootVersion=3.0.1
