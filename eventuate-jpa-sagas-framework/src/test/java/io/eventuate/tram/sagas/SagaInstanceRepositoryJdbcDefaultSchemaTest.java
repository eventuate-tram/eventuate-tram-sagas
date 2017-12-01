package io.eventuate.tram.sagas;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepositoryJdbc;

public class SagaInstanceRepositoryJdbcDefaultSchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc();
  }

  @Override
  protected String getExpectedInsertIntoSagaInstance() {
    return String.format("INSERT INTO %s.saga_instance(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json) VALUES(?, ?, ?,?,?,?)", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedInsertIntoSagaInstanceParticipants() {
    return String.format("INSERT INTO %s.saga_instance_participants(saga_type, saga_id, destination, resource) values(?,?,?,?)", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaInstance() {
    return String.format("SELECT * FROM %s.saga_instance WHERE saga_type = ? AND saga_id = ?", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaInstanceParticipants() {
    return String.format("SELECT destination, resource FROM %s.saga_instance_participants WHERE saga_type = ? AND saga_id = ?", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedUpdateSagaInstance() {
    return String.format("UPDATE %s.saga_instance SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ? where saga_type = ? AND saga_id = ?", EventuateSchema.DEFAULT_SCHEMA);
  }
}
