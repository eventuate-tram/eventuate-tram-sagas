ARG EVENTUATE_COMMON_VERSION
FROM eventuateio/eventuate-postgres:$EVENTUATE_COMMON_VERSION
COPY tram-saga-schema.sql /docker-entrypoint-initdb.d
