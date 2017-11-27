package io.eventuate.tram.sagas;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepositoryJdbc;

public class SagaInstanceRepositoryJdbcEmptySchemaTest extends SagaInstanceRepositoryJdbcSchemaTest {

  @Override
  protected SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc() {
    return new SagaInstanceRepositoryJdbc(new EventuateSchema(EventuateSchema.EMPTY_SCHEMA));
  }

  @Override
  protected String getExpectedInsertIntoSagaInstance() {
    return "INSERT INTO saga_instance(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json) VALUES(?, ?, ?,?,?,?)";
  }

  @Override
  protected String getExpectedInsertIntoSagaInstanceParticipants() {
    return "INSERT INTO saga_instance_participants(saga_type, saga_id, destination, resource) values(?,?,?,?)";
  }

  @Override
  protected String getExpectedSelectFromSagaInstance() {
    return "SELECT * FROM saga_instance WHERE saga_type = ? AND saga_id = ?";
  }

  @Override
  protected String getExpectedSelectFromSagaInstanceParticipants() {
    return "SELECT destination, resource FROM saga_instance_participants WHERE saga_type = ? AND saga_id = ?";
  }

  @Override
  protected String getExpectedUpdateSagaInstance() {
    return "UPDATE saga_instance SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ? where saga_type = ? AND saga_id = ?";
  }
}
