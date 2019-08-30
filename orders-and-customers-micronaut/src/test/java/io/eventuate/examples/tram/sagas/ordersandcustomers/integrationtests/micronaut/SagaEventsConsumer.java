package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests.micronaut;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaCompletedSuccesfully;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaRolledBack;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.subscriber.DomainEventEnvelope;
import io.eventuate.tram.events.subscriber.DomainEventHandlers;
import io.eventuate.tram.events.subscriber.DomainEventHandlersBuilder;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class SagaEventsConsumer {

  private LinkedBlockingDeque<DomainEventEnvelope> domainEvents = new LinkedBlockingDeque<>();

  public DomainEventHandlers domainEventHandlers() {
    return DomainEventHandlersBuilder
            .forAggregateType(CreateOrderSaga.class.getName())
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

  public  <T extends DomainEvent> void assertEventReceived(Class<T> domainEventClass, Consumer<T> consumer) {
    Arrays.stream(domainEvents.toArray(new DomainEventEnvelope[0])).filter((DomainEventEnvelope dee) -> {
      if (!domainEventClass.isInstance(dee.getEvent()))
        return false;
      consumer.accept((T) dee.getEvent());
      return true;
    }).findFirst().get();
  }
}
