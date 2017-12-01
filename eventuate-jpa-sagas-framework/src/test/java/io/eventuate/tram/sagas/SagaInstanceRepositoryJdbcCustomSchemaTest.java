package io.eventuate.tram.sagas;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepositoryJdbc;

public class SagaInstanceRepositoryJdbcCustomSchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc(new EventuateSchema("custom"));
  }

  @Override
  protected String getExpectedInsertIntoSagaInstance() {
    return "INSERT INTO custom.saga_instance(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json) VALUES(?, ?, ?,?,?,?)";
  }

  @Override
  protected String getExpectedInsertIntoSagaInstanceParticipants() {
    return "INSERT INTO custom.saga_instance_participants(saga_type, saga_id, destination, resource) values(?,?,?,?)";
  }

  @Override
  protected String getExpectedSelectFromSagaInstance() {
    return "SELECT * FROM custom.saga_instance WHERE saga_type = ? AND saga_id = ?";
  }

  @Override
  protected String getExpectedSelectFromSagaInstanceParticipants() {
    return "SELECT destination, resource FROM custom.saga_instance_participants WHERE saga_type = ? AND saga_id = ?";
  }

  @Override
  protected String getExpectedUpdateSagaInstance() {
    return "UPDATE custom.saga_instance SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ? where saga_type = ? AND saga_id = ?";
  }
}
