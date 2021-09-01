package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.domain.OrderRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.service.CreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.service.OrderSagaService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.service.OrderService;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class OrderConfiguration {

  @Bean
  public OrderSagaService orderSagaService(OrderRepository orderRepository,
                                           ReactiveSagaInstanceFactory sagaInstanceFactory,
                                           CreateOrderSaga createOrderSaga) {
    return new OrderSagaService(orderRepository, sagaInstanceFactory, createOrderSaga);
  }

  @Bean
  public OrderService orderService(OrderRepository orderRepository) {
    return new OrderService(orderRepository);
  }

  @Bean
  public CreateOrderSaga createOrderSaga(OrderService orderService) {
    return new CreateOrderSaga(orderService);
  }
}
