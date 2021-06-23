package io.eventuate.tram.sagas.reactive.common;

import io.eventuate.tram.messaging.common.Message;
import reactor.core.publisher.Mono;

public interface ReactiveSagaLockManager {

  Mono<Boolean> claimLock(String sagaType, String sagaId, String target);

  Mono<Void> stashMessage(String sagaType, String sagaId, String target, Message message);

  Mono<Message> unlock(String sagaId, String target);
}
