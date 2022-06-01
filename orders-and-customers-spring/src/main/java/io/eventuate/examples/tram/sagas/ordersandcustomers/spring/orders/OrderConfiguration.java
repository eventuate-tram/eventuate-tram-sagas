package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaData;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.LocalCreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderCommandHandler;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderService;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.eventuate.tram.spring.optimisticlocking.OptimisticLockingDecoratorConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@EntityScan("io.eventuate.examples.tram.sagas.ordersandcustomers")
@ComponentScan
@Import(OptimisticLockingDecoratorConfiguration.class)
public class OrderConfiguration {

  @Bean
  public OrderService orderService(OrderDao orderDao,
                                   EventuateTransactionTemplate eventuateTransactionTemplate,
                                   SagaInstanceFactory sagaInstanceFactory,
                                   LocalCreateOrderSaga localCreateOrderSaga,
                                   CreateOrderSaga createOrderSaga) {
    return new OrderService(orderDao, eventuateTransactionTemplate, sagaInstanceFactory, localCreateOrderSaga, createOrderSaga);
  }


  @Bean
  public CreateOrderSaga createOrderSaga(DomainEventPublisher domainEventPublisher) {
    return new CreateOrderSaga(domainEventPublisher) {
      @Autowired
      private ApplicationEventPublisher applicationEventPublisher;

      @Override
      public void onStarting(String sagaId, CreateOrderSagaData createOrderSagaData) {
        applicationEventPublisher.publishEvent(new SagaStartedEvent(this, sagaId));
      }

      @Override
      public void onSagaFailed(String sagaId, CreateOrderSagaData createOrderSagaData) {
        applicationEventPublisher.publishEvent(new SagaFailedEvent(this, sagaId));
      }
    };
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
