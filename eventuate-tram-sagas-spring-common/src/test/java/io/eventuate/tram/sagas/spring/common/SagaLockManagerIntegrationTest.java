package io.eventuate.tram.sagas.spring.common;

import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.id.IdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.common.SagaLockManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes=SagaLockManagerIntegrationTestConfiguration.class)
public class SagaLockManagerIntegrationTest {

  @Autowired
  private SagaLockManager sagaLockManager;

  private IdGenerator idGenerator = new ApplicationIdGenerator();

  String sagaType = "mySagaType";

  @Test
  public void shouldClaimLock() {

    String sagaId = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId, target));

  }

  @Test
  public void shouldNotClaimLock() {

    String sagaId1 = idGenerator.genIdAsString();
    String sagaId2 = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId1, target));
    assertFalse(sagaLockManager.claimLock(sagaType, sagaId2, target));

  }

  @Test
  public void shouldStashMessage() {
    String sagaId = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();
    String messageId = idGenerator.genIdAsString();

    Message message = MessageBuilder.withPayload("hello").withHeader(Message.ID, messageId).build();
    sagaLockManager.stashMessage(sagaType, sagaId, target, message);
  }

  @Test
  public void shouldReleaseLockAndUnstashMessage() {

    String sagaId1 = idGenerator.genIdAsString();
    String sagaId2 = idGenerator.genIdAsString();
    String target  = "/target/" + idGenerator.genIdAsString();
    String messageId = idGenerator.genIdAsString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId1, target));
    assertFalse(sagaLockManager.claimLock(sagaType, sagaId2, target));
    String payload = "hello";
    Message message = MessageBuilder.withPayload(payload).withHeader(Message.ID, messageId).build();
    sagaLockManager.stashMessage(sagaType, sagaId2, target, message);

    Optional<Message> unstashedMessage1 = sagaLockManager.unlock(sagaId1, target);
    assertTrue(unstashedMessage1.isPresent());

    assertEquals(messageId, unstashedMessage1.get().getId());
    assertEquals(payload, unstashedMessage1.get().getPayload());

    assertFalse(sagaLockManager.unlock(sagaId2, target).isPresent());

  }


}