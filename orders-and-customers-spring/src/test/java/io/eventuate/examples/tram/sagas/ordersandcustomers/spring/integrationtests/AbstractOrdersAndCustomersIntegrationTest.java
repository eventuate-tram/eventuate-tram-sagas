package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderState;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaCompletedSuccesfully;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaRolledBack;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders.domain.OrderRepository;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractOrdersAndCustomersIntegrationTest {

  @Autowired
  protected CustomerService customerService;

  @Autowired
  protected OrderService orderService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private SagaEventsConsumer sagaEventsConsumer;

  @Test
  public void shouldApproveOrder() {
    Money creditLimit = new Money("200.00");
    Customer customer = customerService.createCustomer("Fred", creditLimit);
    Order order = createOrder(customer);

    assertOrderState(order.getId(), OrderState.APPROVED);

    assertCreateOrderSagaCompletedSuccesfully(order);

  }

  protected void assertCreateOrderSagaCompletedSuccesfully(Order order) {
    Eventually.eventually(() ->
      sagaEventsConsumer.assertEventReceived(CreateOrderSagaCompletedSuccesfully.class, event ->
        assertEquals(order.getId(), (Long)event.getOrderId())));
  }

  protected Order createOrder(Customer customer) {
    return orderService.createOrder(new OrderDetails(customer.getId(), new Money("123.40")));
  }

  @Test
  public void shouldRejectOrder()  {
    Money creditLimit = new Money("15.00");
    Customer customer = customerService.createCustomer("Fred", creditLimit);
    Order order = createOrder(customer);

    assertOrderRejected(order);

    assertCreateOrderSagaRolledBack(order);

  }

  protected void assertOrderRejected(Order order) {
    assertOrderState(order.getId(), OrderState.REJECTED);
  }

  protected void assertCreateOrderSagaRolledBack(Order order) {
    Eventually.eventually(() ->
      sagaEventsConsumer.assertEventReceived(CreateOrderSagaRolledBack.class, event ->
        assertEquals(order.getId(), (Long)event.getOrderId())));
  }

  private void assertOrderState(Long id, OrderState expectedState) {

    Eventually.eventually(120, 500, TimeUnit.MILLISECONDS, () -> {
      Order order = transactionTemplate.execute(s -> orderRepository.findById(id).get());
      assertEquals(expectedState, order.getState());
    });

  }
}
