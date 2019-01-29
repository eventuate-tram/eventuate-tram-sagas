package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderState;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaCompletedSuccesfully;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaRolledBack;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderService;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertEquals;

public abstract class AbstractOrdersAndCustomersIntegrationTest {

  @Autowired
  private CustomerService customerService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private SagaEventsConsumer sagaEventsConsumer;

  @Test
  public void shouldApproveOrder() {
    Money creditLimit = new Money("15.00");
    Customer customer = customerService.createCustomer("Fred", creditLimit);
    Order order = orderService.createOrder(new OrderDetails(customer.getId(), new Money("12.34")));

    assertOrderState(order.getId(), OrderState.APPROVED);

    Eventually.eventually(() -> {
      sagaEventsConsumer.assertEventReceived(CreateOrderSagaCompletedSuccesfully.class, event -> {
        assertEquals(order.getId(), (Long)event.getOrderId());
      });
    });

  }

  @Test
  public void shouldRejectOrder()  {
    Money creditLimit = new Money("15.00");
    Customer customer = customerService.createCustomer("Fred", creditLimit);
    Order order = orderService.createOrder(new OrderDetails(customer.getId(), new Money("123.40")));

    assertOrderState(order.getId(), OrderState.REJECTED);

    Eventually.eventually(() -> {
      sagaEventsConsumer.assertEventReceived(CreateOrderSagaRolledBack.class, event -> {
        assertEquals(order.getId(), (Long)event.getOrderId());
      });
    });

  }

  private void assertOrderState(Long id, OrderState expectedState) {

    Eventually.eventually(() -> {
      Order order = transactionTemplate.execute(s -> orderRepository.findById(id).get());
      assertEquals(expectedState, order.getState());
    });

  }
}
