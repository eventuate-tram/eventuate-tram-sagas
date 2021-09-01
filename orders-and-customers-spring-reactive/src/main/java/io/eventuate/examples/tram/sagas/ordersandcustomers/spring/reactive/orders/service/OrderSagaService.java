package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.common.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.createorder.CreateOrderSagaData;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.domain.OrderRepository;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

public class OrderSagaService {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ReactiveSagaInstanceFactory sagaInstanceFactory;

  @Autowired
  private CreateOrderSaga createOrderSaga;

  @Autowired
  private TransactionalOperator transactionalOperator;

  public OrderSagaService(OrderRepository orderRepository,
                          ReactiveSagaInstanceFactory sagaInstanceFactory,
                          CreateOrderSaga createOrderSaga) {
    this.orderRepository = orderRepository;
    this.sagaInstanceFactory = sagaInstanceFactory;
    this.createOrderSaga = createOrderSaga;
  }

  public Mono<Order> createOrder(OrderDetails orderDetails) {
    CreateOrderSagaData data = new CreateOrderSagaData(orderDetails);
    return sagaInstanceFactory
            .create(createOrderSaga, data)
            .flatMap(instance -> orderRepository.findById(data.getOrderId()))
            .as(transactionalOperator::transactional);
  }
}
