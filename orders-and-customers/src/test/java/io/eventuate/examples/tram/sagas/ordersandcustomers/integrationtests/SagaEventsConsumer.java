package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaCompletedSuccesfully;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaRolledBack;
import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.events.subscriber.DomainEventHandlersBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;

public class SagaEventsConsumer {

  @Autowired
  private ChannelMapping channelMapping;

  private LinkedBlockingDeque<DomainEventEnvelope> domainEvents = new LinkedBlockingDeque<>();

  public DomainEventHandlers domainEventHandlers() {
    return DomainEventHandlersBuilder
            .forAggregateType(channelMapping.transform(CreateOrderSaga.class.getName()))
            .onEvent(CreateOrderSagaCompletedSuccesfully.class, this::sagaCompleted)
            .onEvent(CreateOrderSagaRolledBack.class, this::sagaRolledBack)
            .build();
  }

  private void sagaCompleted(DomainEventEnvelope<CreateOrderSagaCompletedSuccesfully> dee) {
    domainEvents.add(dee);
  }

  private void sagaRolledBack(DomainEventEnvelope<CreateOrderSagaRolledBack> dee) {
    domainEvents.add(dee);
  }

  public void assertEventReceived(Class<? extends DomainEvent> domainEventClass) {
    Arrays.stream(domainEvents.toArray(new DomainEventEnvelope[0])).filter(dee -> domainEventClass.isInstance(dee.getEvent())).findFirst().get();
  }
}
