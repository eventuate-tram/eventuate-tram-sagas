package io.eventuate.tram.sagas.common;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaLockManagerImplDefaultSchemaTest extends SagaLockManagerImplSchemaTest {

  @Override
  protected SagaLockManagerImpl getSagaLockManager() {
    return new SagaLockManagerImpl();
  }

  @Override
  protected String getExpectedInsertIntoSagaLockTable() {
    return String.format("INSERT INTO %s.saga_lock_table(target, saga_type, saga_id) VALUES(?, ?,?)", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedInsertIntoSagaStashTable() {
    return String.format("INSERT INTO %s.saga_stash_table(message_id, target, saga_type, saga_id, message_headers, message_payload) VALUES(?, ?,?, ?, ?, ?)", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaLockTable() {
    return String.format("select saga_id from %s.saga_lock_table WHERE target = ? FOR UPDATE", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaStashTable() {
    return String.format("select message_id, target, saga_type, saga_id, message_headers, message_payload from %s.saga_stash_table WHERE target = ? ORDER BY message_id LIMIT 1", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedUpdateSagaLockTable() {
    return String.format("update %s.saga_lock_table set saga_type = ?, saga_id = ? where target = ?", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedDeleteFromSagaLockTable() {
    return String.format("delete from %s.saga_lock_table where target = ?", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedDeleteFromSagaStashTable() {
    return String.format("delete from %s.saga_stash_table where message_id = ?", EventuateSchema.DEFAULT_SCHEMA);
  }
}
