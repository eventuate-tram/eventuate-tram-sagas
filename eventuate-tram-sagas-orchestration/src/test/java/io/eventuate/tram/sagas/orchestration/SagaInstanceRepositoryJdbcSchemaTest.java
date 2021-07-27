package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.sagas.common.SagaInstanceRepositorySql;
import org.junit.Assert;
import org.junit.Test;

public abstract class SagaInstanceRepositoryJdbcSchemaTest {
  @Test
  public void testInsertIntoSagaInstance() {
    Assert.assertEquals(getExpectedInsertIntoSagaInstance(), getSagaInstanceRepositoryJdbcSql().getInsertIntoSagaInstanceSql());
  }

  @Test
  public void testInsertIntoSagaInstanceParticipants() {
    Assert.assertEquals(getExpectedInsertIntoSagaInstanceParticipants(), getSagaInstanceRepositoryJdbcSql().getInsertIntoSagaInstanceParticipantsSql());
  }

  @Test
  public void testSelectFromSagaInstance() {
    Assert.assertEquals(getExpectedSelectFromSagaInstance(), getSagaInstanceRepositoryJdbcSql().getSelectFromSagaInstanceSql());
  }

  @Test
  public void testSelectFromSagaInstanceParticipants() {
    Assert.assertEquals(getExpectedSelectFromSagaInstanceParticipants(), getSagaInstanceRepositoryJdbcSql().getSelectFromSagaInstanceParticipantsSql());
  }

  @Test
  public void testUpdateSagaInstance() {
    Assert.assertEquals(getExpectedUpdateSagaInstance(), getSagaInstanceRepositoryJdbcSql().getUpdateSagaInstanceSql());
  }

  protected abstract SagaInstanceRepositorySql getSagaInstanceRepositoryJdbcSql();

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
