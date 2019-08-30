package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests.micronaut;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderState;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaCompletedSuccesfully;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSagaRolledBack;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderService;
import io.eventuate.util.test.async.Eventually;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public abstract class AbstractOrdersAndCustomersIntegrationTest {

  @Inject
  protected CustomerService customerService;

  @Inject
  protected OrderService orderService;

  @Inject
  private OrderDao orderRepository;

  @Inject
  private TransactionTemplate transactionTemplate;

  @Inject
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
    Eventually.eventually(() -> {
      sagaEventsConsumer.assertEventReceived(CreateOrderSagaCompletedSuccesfully.class, event -> {
        assertEquals(order.getId(), (Long)event.getOrderId());
      });
    });
  }

  protected Order createOrder(Customer customer) {
    return orderService.createOrder(new OrderDetails(customer.getId(), new Money("123.40")));
  }

  @Test
  public void shouldRejectOrder()  {
    Money creditLimit = new Money("15.00");
    Customer customer = customerService.createCustomer("Fred", creditLimit);
    Order order = createOrder(customer);

    assertOrderState(order.getId(), OrderState.REJECTED);

    assertCreateOrderSagaRolledBack(order);

  }

  protected void assertCreateOrderSagaRolledBack(Order order) {
    Eventually.eventually(() -> {
      sagaEventsConsumer.assertEventReceived(CreateOrderSagaRolledBack.class, event -> {
        assertEquals(order.getId(), (Long)event.getOrderId());
      });
    });
  }

  private void assertOrderState(Long id, OrderState expectedState) {

    Eventually.eventually(120, 500, TimeUnit.MILLISECONDS, () -> {
      Order order = transactionTemplate.execute(s -> orderRepository.findById(id));
      assertEquals(expectedState, order.getState());
    });

  }
}
