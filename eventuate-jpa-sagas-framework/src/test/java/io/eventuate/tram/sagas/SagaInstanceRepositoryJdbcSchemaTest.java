package io.eventuate.tram.sagas;

import io.eventuate.tram.sagas.orchestration.SagaInstanceRepositoryJdbc;
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

  protected abstract String getExpectedInsertIntoSagaInstance();
  protected abstract String getExpectedInsertIntoSagaInstanceParticipants();
  protected abstract String getExpectedSelectFromSagaInstance();
  protected abstract String getExpectedSelectFromSagaInstanceParticipants();
  protected abstract String getExpectedUpdateSagaInstance();
}
