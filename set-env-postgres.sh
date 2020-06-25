. ./set-env.sh

export SPRING_DATASOURCE_URL=jdbc:postgresql://${DOCKER_HOST_IP}/eventuate
export SPRING_DATASOURCE_USERNAME=eventuate
export SPRING_DATASOURCE_PASSWORD=eventuate
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

export DB_URL=jdbc:postgresql://${DOCKER_HOST_IP}/eventuate
export DB_USERNAME=eventuate
export DB_PASSWORD=eventuate
export DB_DRIVERCLASSNAME=org.postgresql.Driver