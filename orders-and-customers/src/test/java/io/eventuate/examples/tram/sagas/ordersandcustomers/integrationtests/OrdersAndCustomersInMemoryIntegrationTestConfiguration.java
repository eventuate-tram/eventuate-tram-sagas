package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests;

import io.eventuate.tram.inmemory.TramInMemoryConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrdersAndCustomersIntegrationCommonIntegrationTestConfiguration.class, TramInMemoryConfiguration.class})
public class OrdersAndCustomersInMemoryIntegrationTestConfiguration {
}
