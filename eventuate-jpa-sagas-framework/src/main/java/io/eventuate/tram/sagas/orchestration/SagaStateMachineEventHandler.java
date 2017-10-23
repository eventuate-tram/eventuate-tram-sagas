package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;

public interface SagaStateMachineEventHandler<Data, EventClass extends DomainEvent> {

  SagaActions<Data> apply(Data data, DomainEventEnvelope<EventClass> event);


}
