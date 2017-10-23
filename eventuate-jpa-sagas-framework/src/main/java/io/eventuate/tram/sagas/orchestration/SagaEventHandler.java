package io.eventuate.tram.sagas.orchestration;



import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;

import java.util.function.Function;

public class SagaEventHandler<Data> {

  private final Class<DomainEvent> eventClass;
  private final Function<Data, Long> aggregateIdProvider;
  private final SagaStateMachineAction<Data, DomainEventEnvelope<DomainEvent>> action;

  public SagaEventHandler(Class<DomainEvent> eventClass, Function<Data, Long> aggregateIdProvider, SagaStateMachineAction<Data, DomainEventEnvelope<DomainEvent>> action) {
    this.eventClass = eventClass;
    this.aggregateIdProvider = aggregateIdProvider;
    this.action = action;
  }

  public static <Data> SagaEventHandler make(Class<DomainEvent> eventClass, Function<Data, Long> aggregateIdProvider,
                                                                                                SagaStateMachineAction<Data, DomainEventEnvelope<DomainEvent>> eventHandler) {
    return new SagaEventHandler<Data>(eventClass, aggregateIdProvider, eventHandler);
  }


  public EventClassAndAggregateId eventClassAndAggregateId(Data data) {
    return new EventClassAndAggregateId(eventClass, aggregateIdProvider.apply(data));
  }

  public Class<DomainEvent> getEventClass() {
    return eventClass;
  }

  public SagaStateMachineAction<Data, DomainEventEnvelope<DomainEvent>> getAction() {
    return action;
  }
}
