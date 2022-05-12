package io.eventuate.tram.sagas.reactive.orchestration;


import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public interface ReactiveSaga<SAGA_DATA> {

  SagaDefinition<Publisher<SagaActions<SAGA_DATA>>, SAGA_DATA> getSagaDefinition();

  default String getSagaType() {
    return getClass().getName().replace("$", "_DLR_");
  }

  default Mono<Void> onStarting(String sagaId, SAGA_DATA data) {
    return Mono.empty();
  }

  default Mono<Void> onSagaCompletedSuccessfully(String sagaId, SAGA_DATA data) {
    return Mono.empty();
  }

  default Mono<Void> onSagaRolledBack(String sagaId, SAGA_DATA data) {
    return Mono.empty();
  }
}
