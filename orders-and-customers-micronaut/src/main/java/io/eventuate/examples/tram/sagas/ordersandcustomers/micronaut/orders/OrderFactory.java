package io.eventuate.examples.tram.sagas.ordersandcustomers.micronaut.orders;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaData;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.LocalCreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.LocalCreateOrderSagaData;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderCommandHandler;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderService;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.*;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.micronaut.context.annotation.Factory;

import javax.inject.Named;
import javax.inject.Singleton;

@Factory
public class OrderFactory {

  @Singleton
  public OrderService orderService(@Named("createOrderSagaManager") SagaManager<CreateOrderSagaData> createOrderSagaManager,
                                   @Named("localCreateOrderSagaManager") SagaManager<LocalCreateOrderSagaData> localCreateOrderSagaManager,
                                   OrderDao orderDao,
                                   EventuateTransactionTemplate eventuateTransactionTemplate) {
    return new OrderService(createOrderSagaManager, localCreateOrderSagaManager, orderDao, eventuateTransactionTemplate);
  }


  @Singleton
  @Named("createOrderSagaManager")
  public SagaManager<CreateOrderSagaData> createOrderSagaManager(@Named("createOrderSaga") Saga<CreateOrderSagaData> saga,
                                                                 SagaInstanceRepository sagaInstanceRepository,

                                                                 CommandProducer commandProducer,
                                                                 MessageConsumer messageConsumer,
                                                                 SagaLockManager sagaLockManager,
                                                                 SagaCommandProducer sagaCommandProducer) {
    return new SagaManagerImpl<>(saga,
            sagaInstanceRepository,
            commandProducer,
            messageConsumer,
            sagaLockManager,
            sagaCommandProducer);
  }

  @Singleton
  @Named("localCreateOrderSagaManager")
  public SagaManager<LocalCreateOrderSagaData> localCreateOrderSagaManager(@Named("localCreateOrderSaga") Saga<LocalCreateOrderSagaData> saga,
                                                                           SagaInstanceRepository sagaInstanceRepository,
                                                                           CommandProducer commandProducer,
                                                                           MessageConsumer messageConsumer,
                                                                           SagaLockManager sagaLockManager,
                                                                           SagaCommandProducer sagaCommandProducer) {
    return new SagaManagerImpl<>(saga,
            sagaInstanceRepository,
            commandProducer,
            messageConsumer,
            sagaLockManager,
            sagaCommandProducer);
  }


  @Singleton
  @Named("createOrderSaga")
  public CreateOrderSaga createOrderSaga(DomainEventPublisher domainEventPublisher) {
    return new CreateOrderSaga(domainEventPublisher);
  }

  @Singleton
  @Named("localCreateOrderSaga")
  public LocalCreateOrderSaga localCreateOrderSaga(DomainEventPublisher domainEventPublisher, OrderDao orderDao) {
    return new LocalCreateOrderSaga(domainEventPublisher, orderDao);
  }

  @Singleton
  public OrderCommandHandler orderCommandHandler(OrderDao orderDao) {
    return new OrderCommandHandler(orderDao);
  }

  @Singleton
  public CommandDispatcher orderCommandDispatcher(OrderCommandHandler target, SagaCommandDispatcherFactory sagaCommandDispatcherFactory) {
    return sagaCommandDispatcherFactory.make("orderCommandDispatcher", target.commandHandlerDefinitions());
  }

}
