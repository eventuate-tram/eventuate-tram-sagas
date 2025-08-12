package io.eventuate.tram.sagas.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class SagaLockManagerImplSchemaTest {

  @Test
  public void testInsertIntoSagaLockTable() {
    Assertions.assertEquals(getExpectedInsertIntoSagaLockTable(), getSagaLockManagerSql().getInsertIntoSagaLockTableSql());
  }

  @Test
  public void testInsertIntoSagaStashTable() {
    Assertions.assertEquals(getExpectedInsertIntoSagaStashTable(), getSagaLockManagerSql().getInsertIntoSagaStashTableSql());
  }

  @Test
  public void testSelectFromSagaLockTable() {
    Assertions.assertEquals(getExpectedSelectFromSagaLockTable(), getSagaLockManagerSql().getSelectFromSagaLockTableSql());
  }

  @Test
  public void testSelectFromSagaStashTable() {
    Assertions.assertEquals(getExpectedSelectFromSagaStashTable(), getSagaLockManagerSql().getSelectFromSagaStashTableSql());
  }

  @Test
  public void testUpdateSagaLockTable() {
    Assertions.assertEquals(getExpectedUpdateSagaLockTable(), getSagaLockManagerSql().getUpdateSagaLockTableSql());
  }

  @Test
  public void testDeleteFromSagaLockTable() {
    Assertions.assertEquals(getExpectedDeleteFromSagaLockTable(), getSagaLockManagerSql().getDeleteFromSagaLockTableSql());
  }

  @Test
  public void testDeleteFromSagaStashTable() {
    Assertions.assertEquals(getExpectedDeleteFromSagaStashTable(), getSagaLockManagerSql().getDeleteFromSagaStashTableSql());
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
