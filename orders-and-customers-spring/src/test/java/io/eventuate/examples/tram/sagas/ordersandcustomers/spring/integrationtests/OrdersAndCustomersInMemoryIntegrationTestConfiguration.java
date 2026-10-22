package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.common.database.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import io.eventuate.tram.sagas.spring.inmemory.TramSagaInMemoryConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrdersAndCustomersIntegrationCommonIntegrationTestConfiguration.class,
        TramInMemoryConfiguration.class,
        TramSagaInMemoryConfiguration.class,
        EventuateCommonJdbcOperationsConfiguration.class})
public class OrdersAndCustomersInMemoryIntegrationTestConfiguration {
}
