. ./set-env.sh

export SPRING_DATASOURCE_URL=jdbc:mysql://${DOCKER_HOST_IP}/eventuate
export SPRING_DATASOURCE_USERNAME=mysqluser
export SPRING_DATASOURCE_PASSWORD=mysqlpw
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver

export DB_URL=jdbc:mysql://${DOCKER_HOST_IP}/eventuate
export DB_USERNAME=mysqluser
export DB_PASSWORD=mysqlpw
export DB_DRIVERCLASSNAME=com.mysql.jdbc.Driver