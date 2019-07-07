package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests;

import io.eventuate.tram.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrdersAndCustomersIntegrationCommonIntegrationTestConfiguration.class, KafkaConfiguration.class, ActiveMQConfiguration.class})
public class OrdersAndCustomersIntegrationTestConfiguration {
}
