package io.eventuate.tram.sagas.spring.reactive.common;

import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes= ReactiveSagaLockManagerIntegrationTestConfiguration.class)
public class ReactiveSagaLockManagerIntegrationTest {

  @Autowired
  private ReactiveSagaLockManager sagaLockManager;

  @Autowired
  private EventuateSchema eventuateSchema;

  @Autowired
  private EventuateReactiveJdbcStatementExecutor jdbcStatementExecutor;

  private IdGenerator idGenerator = new ApplicationIdGenerator();

  String sagaType = "mySagaType";

  @Test
  public void shouldClaimLock() {

    String sagaId = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId, target).block());
  }

  @Test
  public void shouldNotClaimLock() {

    String sagaId1 = idGenerator.genIdAsString();
    String sagaId2 = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId1, target).block());
    assertFalse(sagaLockManager.claimLock(sagaType, sagaId2, target).block());
  }

  @Test
  public void shouldStashMessage() {
    String sagaId = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();
    String messageId = idGenerator.genIdAsString();

    Message message = MessageBuilder.withPayload("hello").withHeader(Message.ID, messageId).build();
    sagaLockManager.stashMessage(sagaType, sagaId, target, message).block();

    String sagaStashTable = eventuateSchema.qualifyTable("saga_stash_table");

    List<Map<String, Object>> stashedMessages =
            jdbcStatementExecutor
                    .query("select target, saga_type, saga_id, message_headers, message_payload from " + sagaStashTable + " where message_id = ?", messageId)
                    .collectList()
                    .block();

    Assertions.assertEquals(1, stashedMessages.size());

    Map<String, Object> stashedMessage = stashedMessages.get(0);

    Assertions.assertEquals(message.getPayload(), stashedMessage.get("message_payload"));
    Assertions.assertEquals(JSonMapper.toJson(message.getHeaders()), stashedMessage.get("message_headers"));
    Assertions.assertEquals(sagaId, stashedMessage.get("saga_id"));
    Assertions.assertEquals(sagaType, stashedMessage.get("saga_type"));
    Assertions.assertEquals(target, stashedMessage.get("target"));
  }

  @Test
  public void shouldReleaseLockAndUnstashMessage() {

    String sagaId1 = idGenerator.genIdAsString();
    String sagaId2 = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();
    String messageId = idGenerator.genIdAsString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId1, target).block());
    assertFalse(sagaLockManager.claimLock(sagaType, sagaId2, target).block());
    String payload = "hello";
    Message message = MessageBuilder.withPayload(payload).withHeader(Message.ID, messageId).build();
    sagaLockManager.stashMessage(sagaType, sagaId2, target, message).block();

    Optional<Message> unstashedMessage1 = Optional.ofNullable(sagaLockManager.unlock(sagaId1, target).block());
    assertTrue(unstashedMessage1.isPresent());

    assertEquals(messageId, unstashedMessage1.get().getId());
    assertEquals(payload, unstashedMessage1.get().getPayload());

    assertFalse(Optional.ofNullable(sagaLockManager.unlock(sagaId2, target).block()).isPresent());
  }
}