package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests;

import io.eventuate.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrdersAndCustomersIntegrationCommonIntegrationTestConfiguration.class, TramJdbcKafkaConfiguration.class})
public class OrdersAndCustomersIntegrationTestConfiguration {
}
