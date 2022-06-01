package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaInstanceRepositoryJdbcCustomSchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  private String custom = "custom";

  @Override
  protected SagaInstanceRepositorySql getSagaInstanceRepositoryJdbcSql() {
    return new SagaInstanceRepositorySql(new EventuateSchema(custom));
  }

  @Override
  protected String getExpectedPrefix() {
    return custom + ".";
  }
}
