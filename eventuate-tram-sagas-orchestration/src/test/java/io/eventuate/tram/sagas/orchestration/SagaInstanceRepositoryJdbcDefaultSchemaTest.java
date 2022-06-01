package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaInstanceRepositoryJdbcDefaultSchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositorySql getSagaInstanceRepositoryJdbcSql() {
    return new SagaInstanceRepositorySql(new EventuateSchema());
  }

  @Override
  protected String getExpectedPrefix() {
    return EventuateSchema.DEFAULT_SCHEMA + ".";
  }

}
