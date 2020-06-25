#! /bin/bash -e

docker-compose version
docker version
URL="https://github.com/docker/compose/releases/download/1.25.4/docker-compose-`uname -s`-`uname -m`"
echo Downloading from $URL
curl -L $URL > ~/docker-compose
chmod +x ~/docker-compose
sudo mv ~/docker-compose /usr/local/bin/docker-compose
docker-compose version
