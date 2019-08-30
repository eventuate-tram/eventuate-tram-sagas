package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaInstanceRepositoryJdbcCustomSchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  private String custom = "custom";

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc(null, null, new EventuateSchema(custom));
  }

  @Override
  protected String getExpectedPrefix() {
    return custom + ".";
  }
}
