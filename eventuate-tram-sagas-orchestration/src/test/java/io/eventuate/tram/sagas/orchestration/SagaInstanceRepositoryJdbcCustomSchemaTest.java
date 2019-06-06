package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaInstanceRepositoryJdbcCustomSchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  private String custom = "custom";

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc(new EventuateSchema(custom));
  }

  @Override
  protected String getExpectedPrefix() {
    return custom + ".";
  }
}
