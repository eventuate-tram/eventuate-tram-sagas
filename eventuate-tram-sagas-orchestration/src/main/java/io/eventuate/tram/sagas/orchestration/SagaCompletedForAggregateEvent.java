package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.events.common.DomainEvent;

public class SagaCompletedForAggregateEvent implements DomainEvent {
  public SagaCompletedForAggregateEvent(String sagaId) {
  }
}
