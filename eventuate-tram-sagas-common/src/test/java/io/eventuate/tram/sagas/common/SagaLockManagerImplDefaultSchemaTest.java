package io.eventuate.tram.sagas.common;

import io.eventuate.common.jdbc.EventuateSchema;

public class SagaLockManagerImplDefaultSchemaTest extends SagaLockManagerImplSchemaTest {

  @Override
  protected SagaLockManagerSql getSagaLockManagerSql() {
    return new SagaLockManagerSql(new EventuateSchema());
  }

  @Override
  protected String getExpectedInsertIntoSagaLockTable() {
    return "INSERT INTO %s.saga_lock_table(target, saga_type, saga_id) VALUES(?, ?,?)".formatted(EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedInsertIntoSagaStashTable() {
    return "INSERT INTO %s.saga_stash_table(message_id, target, saga_type, saga_id, message_headers, message_payload) VALUES(?, ?, ?, ?, ?, ?)".formatted(EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaLockTable() {
    return "select saga_id from %s.saga_lock_table WHERE target = ? FOR UPDATE".formatted(EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelectFromSagaStashTable() {
    return "select message_id, target, saga_type, saga_id, message_headers, message_payload from %s.saga_stash_table WHERE target = ? ORDER BY message_id LIMIT 1".formatted(EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedUpdateSagaLockTable() {
    return "update %s.saga_lock_table set saga_type = ?, saga_id = ? where target = ?".formatted(EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedDeleteFromSagaLockTable() {
    return "delete from %s.saga_lock_table where target = ?".formatted(EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedDeleteFromSagaStashTable() {
    return "delete from %s.saga_stash_table where message_id = ?".formatted(EventuateSchema.DEFAULT_SCHEMA);
  }
}
