package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.tram.jdbcactivemq.TramJdbcActiveMQConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Import(TramJdbcActiveMQConfiguration.class)
@Profile("ActiveMQ")
public class ActiveMQConfiguration {
}
