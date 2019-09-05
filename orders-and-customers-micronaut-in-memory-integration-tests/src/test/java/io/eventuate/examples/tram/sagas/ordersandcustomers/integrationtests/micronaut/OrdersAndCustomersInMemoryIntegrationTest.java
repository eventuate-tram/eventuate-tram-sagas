package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests.micronaut;

import io.eventuate.examples.tram.sagas.ordersandcustomers.micronaut.tests.AbstractOrdersAndCustomersIntegrationTest;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest(transactional = false)
public class OrdersAndCustomersInMemoryIntegrationTest extends AbstractOrdersAndCustomersIntegrationTest {
}
