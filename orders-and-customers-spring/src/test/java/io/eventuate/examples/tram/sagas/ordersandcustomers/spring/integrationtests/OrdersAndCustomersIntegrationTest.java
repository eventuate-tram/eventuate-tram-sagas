package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrdersAndCustomersIntegrationTestConfiguration.class)
public class OrdersAndCustomersIntegrationTest extends AbstractOrdersAndCustomersIntegrationTest {
}
