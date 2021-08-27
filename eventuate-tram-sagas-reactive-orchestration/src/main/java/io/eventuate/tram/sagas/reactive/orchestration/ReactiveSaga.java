package io.eventuate.tram.sagas.reactive.orchestration;


import reactor.core.publisher.Mono;

public interface ReactiveSaga<Data> {

  ReactiveSagaDefinition<Data> getSagaDefinition();

  default String getSagaType() {
    return getClass().getName().replace("$", "_DLR_");
  }

  default Mono<Void> onStarting(String sagaId, Data data) {
    return Mono.empty();
  }

  default Mono<Void> onSagaCompletedSuccessfully(String sagaId, Data data) {
    return Mono.empty();
  }

  default Mono<Void> onSagaRolledBack(String sagaId, Data data) {
    return Mono.empty();
  }
}
