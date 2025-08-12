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
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = CustomersAndOrdersIntegrationTest.Config.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomersAndOrdersIntegrationTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());

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

    logger.info("shouldApprove CustomerID={}", customer.getId());

    Order order = orderSagaService.createOrder(new OrderDetails(customer.getId(), new Money("12.34"))).block();

    assertOrderState(order.getId(), Optional.of(OrderState.APPROVED), Optional.empty());
  }

  @Test
  public void shouldReject() {
    Customer customer = customerService.createCustomer(CUSTOMER_NAME, new Money("10.00")).block();

    logger.info("shouldReject CustomerID={}", customer.getId());

    Order order = orderSagaService.createOrder(new OrderDetails(customer.getId(), new Money("12.34"))).block();

    assertOrderState(order.getId(), Optional.of(OrderState.REJECTED), Optional.of(RejectionReason.INSUFFICIENT_CREDIT));
  }

  @Test
  public void shouldThrowLocalException() {
    assertThrows(OrderIsTooBigException.class, () -> {
      Customer customer = customerService.createCustomer(CUSTOMER_NAME, new Money("10000000")).block();

      orderSagaService.createOrder(new OrderDetails(customer.getId(), new Money("1000000"))).block();
    });
  }

  @Test
  public void shouldRejectBecauseOfNonExistingUser() {
    long customerId = System.currentTimeMillis();

    logger.info("shouldRejectBecauseOfNonExistingUser CustomerID={}", customerId);

    Order order = orderSagaService.createOrder(new OrderDetails(customerId, new Money("12.34"))).block();

    assertOrderState(order.getId(), Optional.of(OrderState.REJECTED), Optional.of(RejectionReason.UNKNOWN_CUSTOMER));
  }

  private void assertOrderState(Long id, Optional<OrderState> expectedState, Optional<RejectionReason> expectedRejectionReason) {
    Eventually.eventually(90, 500, TimeUnit.MILLISECONDS, () -> {
      Order order = orderRepository.findById(id).block();

      assertEquals(expectedState.map(Enum::name).orElse(null), order.getState());
      assertEquals(expectedRejectionReason.map(Enum::name).orElse(null), order.getRejectionReason());
    });
  }
}