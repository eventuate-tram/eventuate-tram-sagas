package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.events.common.DomainEvent;

import java.util.Optional;

public interface Saga<Data> {

  SagaDefinition<Data> getSagaDefinition();

  default String getSagaType() {
    return getClass().getName();
  }

  default Optional<DomainEvent> makeSagaCompletedSuccessfullyEvent(Data data) { return Optional.empty(); }
  default Optional<DomainEvent> makeSagaRolledBackEvent(Data data) { return Optional.empty(); }

}
