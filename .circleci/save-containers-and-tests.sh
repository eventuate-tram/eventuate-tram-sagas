#! /bin/bash -e

mkdir -p ~/junit /home/circleci/container-logs
docker ps -a > /home/circleci/container-logs/containers.txt
find . -type f -regex ".*/build/test-results/.*xml" -exec cp {} ~/junit/ \;
sudo bash -c 'find /var/lib/docker/containers -name "*-json.log" -exec cp {} /home/circleci/container-logs \;'
sudo bash -c 'find  /home/circleci/container-logs -type f -exec chown circleci {} \;'
