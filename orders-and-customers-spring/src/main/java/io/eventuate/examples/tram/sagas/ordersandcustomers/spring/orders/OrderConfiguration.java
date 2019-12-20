package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders;

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
import io.eventuate.tram.commands.spring.consumer.TramCommandConsumerConfiguration;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.*;
import io.eventuate.tram.sagas.orchestration.spring.SagaOrchestratorConfiguration;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.eventuate.tram.sagas.participant.spring.SagaParticipantConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@EntityScan("io.eventuate.examples.tram.sagas.ordersandcustomers")
@ComponentScan
@Import({TramCommandConsumerConfiguration.class, SagaOrchestratorConfiguration.class})
public class OrderConfiguration {

  @Bean
  public OrderService orderService(SagaManager<CreateOrderSagaData> createOrderSagaManager,
                                   SagaManager<LocalCreateOrderSagaData> localCreateOrderSagaManager,
                                   OrderDao orderDao,
                                   EventuateTransactionTemplate eventuateTransactionTemplate,
                                   SagaInstanceFactory sagaInstanceFactory,
                                   LocalCreateOrderSaga localCreateOrderSaga) {
    return new OrderService(createOrderSagaManager, orderDao, eventuateTransactionTemplate, sagaInstanceFactory, localCreateOrderSaga);
  }


  @Bean
  public SagaManager<CreateOrderSagaData> createOrderSagaManager(Saga<CreateOrderSagaData> saga,
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

  @Bean
  public SagaManager<LocalCreateOrderSagaData> localCreateOrderSagaManager(Saga<LocalCreateOrderSagaData> saga,
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


  @Bean
  public CreateOrderSaga createOrderSaga(DomainEventPublisher domainEventPublisher) {
    return new CreateOrderSaga(domainEventPublisher);
  }

  @Bean
  public LocalCreateOrderSaga localCreateOrderSaga(DomainEventPublisher domainEventPublisher, OrderDao orderDao) {
    return new LocalCreateOrderSaga(domainEventPublisher, orderDao);
  }

  @Bean
  public OrderCommandHandler orderCommandHandler(OrderDao orderDao) {
    return new OrderCommandHandler(orderDao);
  }

  @Bean
  public CommandDispatcher orderCommandDispatcher(OrderCommandHandler target, SagaCommandDispatcherFactory sagaCommandDispatcherFactory) {
    return sagaCommandDispatcherFactory.make("orderCommandDispatcher", target.commandHandlerDefinitions());
  }

}
