package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.common.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.CustomerConfiguration;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.service.CustomerService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.OrderConfiguration;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.common.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.common.OrderState;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.common.RejectionReason;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.domain.OrderIsTooBigException;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.domain.OrderRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.service.OrderSagaService;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CustomersAndOrdersIntegrationTest.Config.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomersAndOrdersIntegrationTest {

  @Configuration
  @Import({OrderConfiguration.class, CustomerConfiguration.class})
  public static class Config {
  }

  private static final String CUSTOMER_NAME = "John";

  @Autowired
  private CustomerService customerService;

  @Autowired
  private OrderSagaService orderSagaService;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  public void shouldApprove() {
    Customer customer = customerService.createCustomer(CUSTOMER_NAME, new Money("15.00")).block();

    Order order = orderSagaService.createOrder(new OrderDetails(customer.getId(), new Money("12.34"))).block();

    assertOrderState(order.getId(), Optional.of(OrderState.APPROVED), Optional.empty());
  }

  @Test
  public void shouldReject() {
    Customer customer = customerService.createCustomer(CUSTOMER_NAME, new Money("10.00")).block();

    Order order = orderSagaService.createOrder(new OrderDetails(customer.getId(), new Money("12.34"))).block();

    assertOrderState(order.getId(), Optional.of(OrderState.REJECTED), Optional.of(RejectionReason.INSUFFICIENT_CREDIT));
  }

  @Test(expected = OrderIsTooBigException.class)
  public void shouldThrowLocalException() {
    Customer customer = customerService.createCustomer(CUSTOMER_NAME, new Money("10000000")).block();

    orderSagaService.createOrder(new OrderDetails(customer.getId(), new Money("1000000"))).block();
  }

  @Test
  public void shouldRejectBecauseOfNonExistingUser() {
    Order order = orderSagaService.createOrder(new OrderDetails(System.currentTimeMillis(), new Money("12.34"))).block();

    assertOrderState(order.getId(), Optional.of(OrderState.REJECTED), Optional.of(RejectionReason.UNKNOWN_CUSTOMER));
  }

  private void assertOrderState(Long id, Optional<OrderState> expectedState, Optional<RejectionReason> expectedRejectionReason) {
    Eventually.eventually(60, 500, TimeUnit.MILLISECONDS, () -> {
      Order order = orderRepository.findById(id).block();

      assertEquals(expectedState.map(Enum::name).orElse(null), order.getState());
      assertEquals(expectedRejectionReason.map(Enum::name).orElse(null), order.getRejectionReason());
    });
  }
}