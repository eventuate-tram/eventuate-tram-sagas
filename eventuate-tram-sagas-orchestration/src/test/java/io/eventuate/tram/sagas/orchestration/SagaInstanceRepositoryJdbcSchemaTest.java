package io.eventuate.tram.sagas.orchestration;

import org.junit.Assert;
import org.junit.Test;

public abstract class SagaInstanceRepositoryJdbcSchemaTest {
  @Test
  public void testInsertIntoSagaInstance() {
    Assert.assertEquals(getExpectedInsertIntoSagaInstance(), getSagaInstanceRepositoryJdbc().getInsertIntoSagaInstanceSql());
  }

  @Test
  public void testInsertIntoSagaInstanceParticipants() {
    Assert.assertEquals(getExpectedInsertIntoSagaInstanceParticipants(), getSagaInstanceRepositoryJdbc().getInsertIntoSagaInstanceParticipantsSql());
  }

  @Test
  public void testSelectFromSagaInstance() {
    Assert.assertEquals(getExpectedSelectFromSagaInstance(), getSagaInstanceRepositoryJdbc().getSelectFromSagaInstanceSql());
  }

  @Test
  public void testSelectFromSagaInstanceParticipants() {
    Assert.assertEquals(getExpectedSelectFromSagaInstanceParticipants(), getSagaInstanceRepositoryJdbc().getSelectFromSagaInstanceParticipantsSql());
  }

  @Test
  public void testUpdateSagaInstance() {
    Assert.assertEquals(getExpectedUpdateSagaInstance(), getSagaInstanceRepositoryJdbc().getUpdateSagaInstanceSql());
  }

  protected abstract SagaInstanceRepositoryJdbc getSagaInstanceRepositoryJdbc();

  private String getExpectedInsertIntoSagaInstance() {
    return String.format("INSERT INTO %ssaga_instance(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json, end_state, compensating) VALUES(?, ?, ?, ?, ?, ?, ?, ?)", getExpectedPrefix());
  }

  private String getExpectedInsertIntoSagaInstanceParticipants() {
    return String.format("INSERT INTO %ssaga_instance_participants(saga_type, saga_id, destination, resource) values(?,?,?,?)", getExpectedPrefix());
  }

  private String getExpectedSelectFromSagaInstance() {
    return String.format("SELECT * FROM %ssaga_instance WHERE saga_type = ? AND saga_id = ?", getExpectedPrefix());
  }

  private String getExpectedSelectFromSagaInstanceParticipants() {
    return String.format("SELECT destination, resource FROM %ssaga_instance_participants WHERE saga_type = ? AND saga_id = ?", getExpectedPrefix());
  }

  private String getExpectedUpdateSagaInstance() {
    return String.format("UPDATE %ssaga_instance SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ?, end_state = ?, compensating = ? where saga_type = ? AND saga_id = ?", getExpectedPrefix());
  }

  protected abstract String getExpectedPrefix();
}
