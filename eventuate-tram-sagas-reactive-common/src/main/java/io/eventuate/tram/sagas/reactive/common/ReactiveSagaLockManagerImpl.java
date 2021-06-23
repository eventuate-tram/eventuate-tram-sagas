package io.eventuate.tram.sagas.reactive.common;

import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.common.SagaLockManagerSql;
import io.eventuate.tram.sagas.common.StashedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class ReactiveSagaLockManagerImpl implements ReactiveSagaLockManager {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor;
  private SagaLockManagerSql sagaLockManagerSql;

  public ReactiveSagaLockManagerImpl(EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                     EventuateSchema eventuateSchema) {

    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;

    sagaLockManagerSql = new SagaLockManagerSql(eventuateSchema);
  }

  @Override
  public Mono<Boolean> claimLock(String sagaType, String sagaId, String target) {
    return eventuateJdbcStatementExecutor
            .update(sagaLockManagerSql.getInsertIntoSagaLockTableSql(), target, sagaType, sagaId)
            .then(Mono.just(true))
            .onErrorResume(EventuateDuplicateKeyException.class, e -> {
              Mono<String> owningSagaId = selectForUpdate(target);
              return owningSagaId.map(id -> id.equals(sagaId)).switchIfEmpty(claimLock(sagaType, sagaId, target));
            });
  }

  private Mono<String> selectForUpdate(String target) {
    return eventuateJdbcStatementExecutor
            .query(sagaLockManagerSql.getSelectFromSagaLockTableSql(), target)
            .map(row -> row.get("saga_id").toString())
            .collectList()
            .flatMap(ids -> {
              if (ids.isEmpty()) return Mono.empty();
              else return Mono.just(ids.get(0));
            });
  }

  @Override
  public Mono<Void> stashMessage(String sagaType, String sagaId, String target, Message message) {

    return eventuateJdbcStatementExecutor
            .update(sagaLockManagerSql.getInsertIntoSagaStashTableSql(),
                    message.getRequiredHeader(Message.ID),
                    target,
                    sagaType,
                    sagaId,
                    JSonMapper.toJson(message.getHeaders()),
                    message.getPayload())
            .then();
  }

  @Override
  public Mono<Message> unlock(String sagaId, String target) {
    Mono<List<StashedMessage>> stashedMessages = selectForUpdate(target)
            .switchIfEmpty(Mono.error(new RuntimeException("owningSagaId is not present")))
            .flatMap(id -> {
              if (id.equals(sagaId)) return Mono.just(id);
              else return Mono.error(new RuntimeException(String.format("Expected owner to be %s but is %s", sagaId, id)));
            })
            .then(eventuateJdbcStatementExecutor
                    .query(sagaLockManagerSql.getSelectFromSagaStashTableSql(), target)
                    .map(row -> new StashedMessage(row.get("saga_type").toString(), row.get("saga_id").toString(), MessageBuilder
                            .withPayload(row.get("message_payload").toString())
                            .withExtraHeaders("", JSonMapper.fromJson(row.get("message_headers").toString(), Map.class))
                            .build()))
                    .collectList());

    return stashedMessages
            .flatMap(messages -> {
              if (messages.isEmpty()) return Mono.empty();
              else return Mono.just(messages.get(0));
            })
            .flatMap(stashedMessage ->
              eventuateJdbcStatementExecutor
                      .update(sagaLockManagerSql.getUpdateSagaLockTableSql(), stashedMessage.getSagaType(), stashedMessage.getSagaId(), target)
                      .flatMap(count -> assertEqualToOne(count, Mono.just(stashedMessage))))
            .flatMap(stashedMessage ->
              eventuateJdbcStatementExecutor
                      .update(sagaLockManagerSql.getDeleteFromSagaStashTableSql(), stashedMessage.getMessage().getId())
                      .flatMap(count -> assertEqualToOne(count, Mono.just(stashedMessage))))
            .map(StashedMessage::getMessage)
            .switchIfEmpty(eventuateJdbcStatementExecutor
                    .update(sagaLockManagerSql.getDeleteFromSagaLockTableSql(), target)
                    .flatMap(count -> assertEqualToOne(count, Mono.empty())));
  }

  private <T> Mono<T> assertEqualToOne(int n, Mono<T> onSuccess) {
    if (n == 1) return onSuccess;
    else return Mono.error(new RuntimeException("Expected to update one row but updated: " + n));
  }
}
