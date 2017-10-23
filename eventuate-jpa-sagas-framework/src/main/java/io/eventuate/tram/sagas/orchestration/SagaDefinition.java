package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.messaging.common.Message;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SagaDefinition<Data> {


  // Execution

  Optional<StartingHandler<Data>> getStartingHandler();

  Optional<ReplyClassAndHandler> findReplyHandler(Saga<Data> saga, SagaInstance si, String currentState, Data data, String requestId, Message message);

  List<EventClassAndAggregateId> findEventHandlers(Saga<Data> saga, String currentState, Data data);

  Optional<SagaEventHandler<Data>> findEventHandler(Saga<Data> saga, String currentState, Data data, String aggregateType, long aggregateId, String eventType);

  Set<Class<DomainEvent>> getTriggeringEvents();

  Set<Class<DomainEvent>> getHandledEvents();

  boolean isEndState(String state);
}
