package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootTest(classes = OrdersAndCustomersIntegrationTestConfiguration.class)
public class OrdersAndCustomersIntegrationTest extends AbstractOrdersAndCustomersIntegrationTest {
}
