package io.eventuate.tram.sagas.orchestration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class SagaInstanceRepositoryJdbcSchemaTest {
  @Test
  public void testInsertIntoSagaInstance() {
    Assertions.assertEquals(getExpectedInsertIntoSagaInstance(), getSagaInstanceRepositoryJdbcSql().getInsertIntoSagaInstanceSql());
  }

  @Test
  public void testInsertIntoSagaInstanceParticipants() {
    Assertions.assertEquals(getExpectedInsertIntoSagaInstanceParticipants(), getSagaInstanceRepositoryJdbcSql().getInsertIntoSagaInstanceParticipantsSql());
  }

  @Test
  public void testSelectFromSagaInstance() {
    Assertions.assertEquals(getExpectedSelectFromSagaInstance(), getSagaInstanceRepositoryJdbcSql().getSelectFromSagaInstanceSql());
  }

  @Test
  public void testSelectFromSagaInstanceParticipants() {
    Assertions.assertEquals(getExpectedSelectFromSagaInstanceParticipants(), getSagaInstanceRepositoryJdbcSql().getSelectFromSagaInstanceParticipantsSql());
  }

  @Test
  public void testUpdateSagaInstance() {
    Assertions.assertEquals(getExpectedUpdateSagaInstance(), getSagaInstanceRepositoryJdbcSql().getUpdateSagaInstanceSql());
  }

  protected abstract SagaInstanceRepositorySql getSagaInstanceRepositoryJdbcSql();

  private String getExpectedInsertIntoSagaInstance() {
    return "INSERT INTO %ssaga_instance(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json, end_state, compensating, failed) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)".formatted(getExpectedPrefix());
  }

  private String getExpectedInsertIntoSagaInstanceParticipants() {
    return "INSERT INTO %ssaga_instance_participants(saga_type, saga_id, destination, resource) values(?,?,?,?)".formatted(getExpectedPrefix());
  }

  private String getExpectedSelectFromSagaInstance() {
    return "SELECT * FROM %ssaga_instance WHERE saga_type = ? AND saga_id = ?".formatted(getExpectedPrefix());
  }

  private String getExpectedSelectFromSagaInstanceParticipants() {
    return "SELECT destination, resource FROM %ssaga_instance_participants WHERE saga_type = ? AND saga_id = ?".formatted(getExpectedPrefix());
  }

  private String getExpectedUpdateSagaInstance() {
    return "UPDATE %ssaga_instance SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ?, end_state = ?, compensating = ?, failed = ? where saga_type = ? AND saga_id = ?".formatted(getExpectedPrefix());
  }

  protected abstract String getExpectedPrefix();
}
