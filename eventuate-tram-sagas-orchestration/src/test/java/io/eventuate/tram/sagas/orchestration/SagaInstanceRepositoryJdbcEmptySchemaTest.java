package io.eventuate.tram.sagas.orchestration;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;

public class SagaInstanceRepositoryJdbcEmptySchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc(new EventuateSchema(EventuateSchema.EMPTY_SCHEMA));
  }

  @Override
  protected String getExpectedPrefix() {
    return "";
  }

}
