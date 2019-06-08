package io.eventuate.tram.sagas.common;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes=SagaLockManagerIntegrationTestConfiguration.class)
public class SagaLockManagerIntegrationTest {

  @Autowired
  private SagaLockManager sagaLockManager;

  @Autowired
  private IdGenerator idGenerator;

  String sagaType = "mySagaType";

  @Test
  public void shouldClaimLock() {

    String sagaId = idGenerator.genId().toString();
    String target  = "/target/" + idGenerator.genId().toString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId, target));

  }

  @Test
  public void shouldNotClaimLock() {

    String sagaId1 = idGenerator.genId().toString();
    String sagaId2 = idGenerator.genId().toString();
    String target  = "/target/" + idGenerator.genId().toString();

    assertTrue(sagaLockManager.claimLock(sagaType, sagaId1, target));
    assertFalse(sagaLockManager.claimLock(sagaType, sagaId2, target));

  }

  @Test
  public void shouldStashMessage() {
    String sagaId = idGenerator.genId().toString();
    String target  = "/target/" + idGenerator.genId().toString();
    String messageId = idGenerator.genId().toString();

    Message message = MessageBuilder.withPayload("hello").withHeader(Message.ID, messageId).build();
    sagaLockManager.stashMessage(sagaType, sagaId, target, message);
  }

  @Test
  public void shouldReleaseLockAndUnstashMessage() {

    String sagaId1 = idGenerator.genId().toString();
    String sagaId2 = idGenerator.genId().toString();
    String target  = "/target/" + idGenerator.genId().toString();
    String messageId = idGenerator.genId().toString();

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