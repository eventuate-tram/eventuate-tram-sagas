package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrdersAndCustomersInMemoryIntegrationTestConfiguration.class)
public class OrdersAndCustomersLocalSagaInMemoryIntegrationTest extends AbstractOrdersAndCustomersIntegrationTest {

  protected Order createOrder(Customer customer) {
    return orderService.localCreateOrder(new OrderDetails(customer.getId(), new Money("123.40")));
  }


  @Override
  protected void assertCreateOrderSagaCompletedSuccesfully(Order order) {
    // do nothing
  }

  @Override
  protected void assertCreateOrderSagaRolledBack(Order order) {
    // do nothing
  }

}
