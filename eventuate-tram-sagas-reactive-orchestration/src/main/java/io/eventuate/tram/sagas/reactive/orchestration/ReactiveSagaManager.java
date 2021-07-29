package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.tram.sagas.orchestration.SagaInstance;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ReactiveSagaManager<Data> {
  Mono<SagaInstance> create(Data sagaData);
  void subscribeToReplyChannel();
  Mono<SagaInstance> create(Data sagaData, Optional<String> lockTarget);
  Mono<SagaInstance> create(Data data, Class targetClass, Object targetId);
}
