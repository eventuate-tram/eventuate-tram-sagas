package io.eventuate.tram.sagas.participant;

import org.junit.Assert;
import org.junit.Test;

public abstract class SagaLockManagerImplSchemaTest {

  @Test
  public void testInsertIntoSagaLockTable() {
    Assert.assertEquals(getExpectedInsertIntoSagaLockTable(), getSagaLockManager().getInsertIntoSagaLockTableSql());
  }

  @Test
  public void testInsertIntoSagaStashTable() {
    Assert.assertEquals(getExpectedInsertIntoSagaStashTable(), getSagaLockManager().getInsertIntoSagaStashTableSql());
  }

  @Test
  public void testSelectFromSagaLockTable() {
    Assert.assertEquals(getExpectedSelectFromSagaLockTable(), getSagaLockManager().getSelectFromSagaLockTableSql());
  }

  @Test
  public void testSelectFromSagaStashTable() {
    Assert.assertEquals(getExpectedSelectFromSagaStashTable(), getSagaLockManager().getSelectFromSagaStashTableSql());
  }

  @Test
  public void testUpdateSagaLockTable() {
    Assert.assertEquals(getExpectedUpdateSagaLockTable(), getSagaLockManager().getUpdateSagaLockTableSql());
  }

  @Test
  public void testDeleteFromSagaLockTable() {
    Assert.assertEquals(getExpectedDeleteFromSagaLockTable(), getSagaLockManager().getDeleteFromSagaLockTableSql());
  }

  @Test
  public void testDeleteFromSagaStashTable() {
    Assert.assertEquals(getExpectedDeleteFromSagaStashTable(), getSagaLockManager().getDeleteFromSagaStashTableSql());
  }

  protected abstract SagaLockManagerImpl getSagaLockManager();
  protected abstract String getExpectedInsertIntoSagaLockTable();
  protected abstract String getExpectedInsertIntoSagaStashTable();
  protected abstract String getExpectedSelectFromSagaLockTable();
  protected abstract String getExpectedSelectFromSagaStashTable();
  protected abstract String getExpectedUpdateSagaLockTable();
  protected abstract String getExpectedDeleteFromSagaStashTable();
  protected abstract String getExpectedDeleteFromSagaLockTable();
}
