package io.eventuate.tram.sagas.common;

import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SagaLockManagerImpl implements SagaLockManager {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;

  private SagaLockManagerSql sagaLockManagerSql;

  public SagaLockManagerImpl(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor, EventuateSchema eventuateSchema) {
    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;

    sagaLockManagerSql = new SagaLockManagerSql(eventuateSchema);
  }

  @Override
  public boolean claimLock(String sagaType, String sagaId, String target) {
    while (true)
      try {
        eventuateJdbcStatementExecutor.update(sagaLockManagerSql.getInsertIntoSagaLockTableSql(), target, sagaType, sagaId);
        logger.debug("Saga {} {} has locked {}", sagaType, sagaId, target);
        return true;
      } catch (EventuateDuplicateKeyException e) {
        Optional<String> owningSagaId = selectForUpdate(target);
        if (owningSagaId.isPresent()) {
          if (owningSagaId.get().equals(sagaId))
            return true;
          else {
            logger.debug("Saga {} {} is blocked by {} which has locked {}", sagaType, sagaId, owningSagaId, target);
            return false;
          }
        }
        logger.debug("{}  is repeating attempt to lock {}", sagaId, target);
      }
  }

  private Optional<String> selectForUpdate(String target) {
    return eventuateJdbcStatementExecutor
            .query(sagaLockManagerSql.getSelectFromSagaLockTableSql(), (rs, rowNum) -> rs.getString("saga_id"), target).stream().findFirst();
  }

  @Override
  public void stashMessage(String sagaType, String sagaId, String target, Message message) {

    logger.debug("Stashing message from {} for {} : {}", sagaId, target, message);

    eventuateJdbcStatementExecutor.update(sagaLockManagerSql.getInsertIntoSagaStashTableSql(),
            message.getRequiredHeader(Message.ID),
            target,
            sagaType,
            sagaId,
            JSonMapper.toJson(message.getHeaders()),
            message.getPayload());
  }

  @Override
  public Optional<Message> unlock(String sagaId, String target) {
    Optional<String> owningSagaId = selectForUpdate(target);

    if (owningSagaId.isEmpty()) {
      throw new RuntimeException("owningSagaId is not present");
    }

    if (!owningSagaId.get().equals(sagaId)) {
      throw new RuntimeException("Expected owner to be %s but is %s".formatted(sagaId, owningSagaId.get()));
    }

    logger.debug("Saga {} has unlocked {}", sagaId, target);

    List<StashedMessage> stashedMessages = eventuateJdbcStatementExecutor.query(sagaLockManagerSql.getSelectFromSagaStashTableSql(), (rs, rowNum) -> {
      return new StashedMessage(rs.getString("saga_type"), rs.getString("saga_id"),
              MessageBuilder.withPayload(rs.getString("message_payload")).withExtraHeaders("",
                      JSonMapper.fromJson(rs.getString("message_headers"), Map.class)).build());
    }, target);

    if (stashedMessages.isEmpty()) {
      assertEqualToOne(eventuateJdbcStatementExecutor.update(sagaLockManagerSql.getDeleteFromSagaLockTableSql(), target));
      return Optional.empty();
    }

    StashedMessage stashedMessage = stashedMessages.get(0);

    logger.debug("unstashed from {}  for {} : {}", sagaId, target, stashedMessage.getMessage());

    assertEqualToOne(eventuateJdbcStatementExecutor.update(sagaLockManagerSql.getUpdateSagaLockTableSql(), stashedMessage.getSagaType(),
            stashedMessage.getSagaId(), target));
    assertEqualToOne(eventuateJdbcStatementExecutor.update(sagaLockManagerSql.getDeleteFromSagaStashTableSql(), stashedMessage.getMessage().getId()));

    return Optional.of(stashedMessage.getMessage());
  }

  private void assertEqualToOne(int n) {
    if (n != 1)
      throw new RuntimeException("Expected to update one row but updated: " + n);
  }
}
