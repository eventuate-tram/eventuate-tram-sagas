package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.tram.jdbckafka.TramJdbcKafkaConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Import(TramJdbcKafkaConfiguration.class)
@Profile("!ActiveMQ")
public class KafkaConfiguration {
}
