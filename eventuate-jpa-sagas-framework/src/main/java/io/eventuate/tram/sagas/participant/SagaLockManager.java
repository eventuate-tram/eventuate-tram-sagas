package io.eventuate.tram.sagas.participant;

import io.eventuate.tram.messaging.common.Message;

import java.util.Optional;

public interface SagaLockManager {

  boolean claimLock(String sagaType, String sagaId, String target);

  void stashMessage(String sagaType, String sagaId, String target, Message message);

  Optional<Message> unlock(String sagaId, String target);
}
