package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaInstanceRepositoryJdbcEmptySchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc(null, null, new EventuateSchema(EventuateSchema.EMPTY_SCHEMA));
  }

  @Override
  protected String getExpectedPrefix() {
    return "";
  }

}
