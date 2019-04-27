package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;

public interface EventStartingHandler<Data, EventClass extends DomainEvent> {
  void apply(Data data, DomainEventEnvelope<EventClass> event);
}
