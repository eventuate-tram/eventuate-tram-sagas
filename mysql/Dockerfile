ARG EVENTUATE_COMMON_VERSION
FROM eventuateio/eventuate-mysql8:$EVENTUATE_COMMON_VERSION
COPY tram-saga-schema.sql /docker-entrypoint-initdb.d
