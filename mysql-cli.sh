#! /bin/bash -e

docker run ${1:--it} \
   --name mysqlterm --network=${PWD##*/}_default --rm \
   -e MYSQL_HOST=mysql \
   mysql/mysql-server:8.0.27-1.2.6-server \
   sh -c 'exec mysql -h"$MYSQL_HOST"  -uroot -prootpassword -o eventuate'
