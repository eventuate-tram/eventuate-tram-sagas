#! /bin/bash -e

mkdir -p ~/junit ~/container-logs
docker ps -a > ~/container-logs/containers.txt
find . -type f -regex ".*/build/test-results/.*xml" -exec cp {} ~/junit/ \;
sudo bash -c 'find /var/lib/docker/containers -name "*-json.log" -exec cp {} ~/container-logs \;'
sudo bash -c 'find  ~/container-logs -type f -exec chown circleci {} \;'
