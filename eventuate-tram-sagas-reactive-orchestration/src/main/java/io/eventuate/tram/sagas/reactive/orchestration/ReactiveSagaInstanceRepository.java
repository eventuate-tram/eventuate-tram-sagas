package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.tram.sagas.orchestration.SagaInstance;
import reactor.core.publisher.Mono;

public interface ReactiveSagaInstanceRepository {

  Mono<Void> save(SagaInstance sagaInstance);
  Mono<SagaInstance> find(String sagaType, String sagaId);
  Mono<Void> update(SagaInstance sagaInstance);
}
