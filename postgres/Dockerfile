ARG EVENTUATE_TRAM_IMAGE_VERSION
FROM eventuateio/eventuate-tram-postgres:$EVENTUATE_TRAM_IMAGE_VERSION
COPY tram-saga-schema.sql /docker-entrypoint-initdb.d
