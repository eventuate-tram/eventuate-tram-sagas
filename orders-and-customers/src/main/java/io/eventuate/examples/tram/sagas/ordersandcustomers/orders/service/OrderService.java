package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service;

import io.eventuate.common.jdbc.EventuateTransactionTemplate;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaData;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.LocalCreateOrderSagaData;
import io.eventuate.tram.events.publisher.ResultWithEvents;
import io.eventuate.tram.sagas.orchestration.SagaManager;

public class OrderService {

  private SagaManager<CreateOrderSagaData> createOrderSagaManager;
  private SagaManager<LocalCreateOrderSagaData> localCreateOrderSagaManager;
  private OrderDao orderRepository;
  private EventuateTransactionTemplate eventuateTransactionTemplate;

  public OrderService(SagaManager<CreateOrderSagaData> createOrderSagaManager,
                      SagaManager<LocalCreateOrderSagaData> localCreateOrderSagaManager,
                      OrderDao orderDao,
                      EventuateTransactionTemplate eventuateTransactionTemplate) {
    this.createOrderSagaManager = createOrderSagaManager;
    this.localCreateOrderSagaManager = localCreateOrderSagaManager;
    this.orderRepository = orderDao;
    this.eventuateTransactionTemplate = eventuateTransactionTemplate;
  }

  public Order createOrder(OrderDetails orderDetails) {
    return eventuateTransactionTemplate.executeInTransaction(() -> {
      ResultWithEvents<Order> oe = Order.createOrder(orderDetails);
      Order order = oe.result;
      orderRepository.save(order);
      CreateOrderSagaData data = new CreateOrderSagaData(order.getId(), orderDetails);
      createOrderSagaManager.create(data, Order.class, order.getId());
      return order;
    });
  }

  public Order localCreateOrder(OrderDetails orderDetails) {
    return eventuateTransactionTemplate.executeInTransaction(() -> {
      LocalCreateOrderSagaData data = new LocalCreateOrderSagaData(orderDetails);
      localCreateOrderSagaManager.create(data);
      return orderRepository.findById(data.getOrderId());
    });
  }

}
