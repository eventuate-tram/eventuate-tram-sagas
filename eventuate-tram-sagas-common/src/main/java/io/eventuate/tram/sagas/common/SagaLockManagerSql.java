package io.eventuate.tram.sagas.common;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaLockManagerSql {

  private String insertIntoSagaLockTableSql;
  private String insertIntoSagaStashTableSql;
  private String selectFromSagaLockTableSql;
  private String selectFromSagaStashTableSql;
  private String updateSagaLockTableSql;
  private String deleteFromSagaLockTableSql;
  private String deleteFromSagaStashTableSql;

  public SagaLockManagerSql(EventuateSchema eventuateSchema) {

    String sagaLockTable = eventuateSchema.qualifyTable("saga_lock_table");
    String sagaStashTable = eventuateSchema.qualifyTable("saga_stash_table");

    insertIntoSagaLockTableSql = "INSERT INTO %s(target, saga_type, saga_id) VALUES(?, ?,?)".formatted(sagaLockTable);
    insertIntoSagaStashTableSql = "INSERT INTO %s(message_id, target, saga_type, saga_id, message_headers, message_payload) VALUES(?, ?, ?, ?, ?, ?)".formatted(sagaStashTable);
    selectFromSagaLockTableSql = "select saga_id from %s WHERE target = ? FOR UPDATE".formatted(sagaLockTable);
    selectFromSagaStashTableSql = "select message_id, target, saga_type, saga_id, message_headers, message_payload from %s WHERE target = ? ORDER BY message_id LIMIT 1".formatted(sagaStashTable);
    updateSagaLockTableSql = "update %s set saga_type = ?, saga_id = ? where target = ?".formatted(sagaLockTable);
    deleteFromSagaLockTableSql = "delete from %s where target = ?".formatted(sagaLockTable);
    deleteFromSagaStashTableSql = "delete from %s where message_id = ?".formatted(sagaStashTable);
  }

  public String getInsertIntoSagaLockTableSql() {
    return insertIntoSagaLockTableSql;
  }

  public String getInsertIntoSagaStashTableSql() {
    return insertIntoSagaStashTableSql;
  }

  public String getSelectFromSagaLockTableSql() {
    return selectFromSagaLockTableSql;
  }

  public String getSelectFromSagaStashTableSql() {
    return selectFromSagaStashTableSql;
  }

  public String getUpdateSagaLockTableSql() {
    return updateSagaLockTableSql;
  }

  public String getDeleteFromSagaLockTableSql() {
    return deleteFromSagaLockTableSql;
  }

  public String getDeleteFromSagaStashTableSql() {
    return deleteFromSagaStashTableSql;
  }
}
