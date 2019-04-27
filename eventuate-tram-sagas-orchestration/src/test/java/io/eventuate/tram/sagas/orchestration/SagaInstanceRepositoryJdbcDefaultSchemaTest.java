package io.eventuate.tram.sagas.orchestration;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;

public class SagaInstanceRepositoryJdbcDefaultSchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc(new EventuateSchema());
  }

  @Override
  protected String getExpectedPrefix() {
    return EventuateSchema.DEFAULT_SCHEMA + ".";
  }

}
