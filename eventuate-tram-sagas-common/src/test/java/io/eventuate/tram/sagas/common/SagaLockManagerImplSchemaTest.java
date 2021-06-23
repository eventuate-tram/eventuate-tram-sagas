package io.eventuate.tram.sagas.common;

import org.junit.Assert;
import org.junit.Test;

public abstract class SagaLockManagerImplSchemaTest {

  @Test
  public void testInsertIntoSagaLockTable() {
    Assert.assertEquals(getExpectedInsertIntoSagaLockTable(), getSagaLockManagerSql().getInsertIntoSagaLockTableSql());
  }

  @Test
  public void testInsertIntoSagaStashTable() {
    Assert.assertEquals(getExpectedInsertIntoSagaStashTable(), getSagaLockManagerSql().getInsertIntoSagaStashTableSql());
  }

  @Test
  public void testSelectFromSagaLockTable() {
    Assert.assertEquals(getExpectedSelectFromSagaLockTable(), getSagaLockManagerSql().getSelectFromSagaLockTableSql());
  }

  @Test
  public void testSelectFromSagaStashTable() {
    Assert.assertEquals(getExpectedSelectFromSagaStashTable(), getSagaLockManagerSql().getSelectFromSagaStashTableSql());
  }

  @Test
  public void testUpdateSagaLockTable() {
    Assert.assertEquals(getExpectedUpdateSagaLockTable(), getSagaLockManagerSql().getUpdateSagaLockTableSql());
  }

  @Test
  public void testDeleteFromSagaLockTable() {
    Assert.assertEquals(getExpectedDeleteFromSagaLockTable(), getSagaLockManagerSql().getDeleteFromSagaLockTableSql());
  }

  @Test
  public void testDeleteFromSagaStashTable() {
    Assert.assertEquals(getExpectedDeleteFromSagaStashTable(), getSagaLockManagerSql().getDeleteFromSagaStashTableSql());
  }

  protected abstract SagaLockManagerSql getSagaLockManagerSql();
  protected abstract String getExpectedInsertIntoSagaLockTable();
  protected abstract String getExpectedInsertIntoSagaStashTable();
  protected abstract String getExpectedSelectFromSagaLockTable();
  protected abstract String getExpectedSelectFromSagaStashTable();
  protected abstract String getExpectedUpdateSagaLockTable();
  protected abstract String getExpectedDeleteFromSagaStashTable();
  protected abstract String getExpectedDeleteFromSagaLockTable();
}
