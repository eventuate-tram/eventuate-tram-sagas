package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = OrdersAndCustomersInMemoryIntegrationTestConfiguration.class)
public class OrdersAndCustomersInMemoryIntegrationTest extends AbstractOrdersAndCustomersIntegrationTest {
}
