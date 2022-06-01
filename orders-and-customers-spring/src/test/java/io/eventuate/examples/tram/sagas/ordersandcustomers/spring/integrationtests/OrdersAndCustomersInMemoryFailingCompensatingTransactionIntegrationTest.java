package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSaga;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderCommandHandler;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderService;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders.SagaFailedEvent;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders.SagaStartedEvent;
import io.eventuate.tram.sagas.orchestration.SagaInstance;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepository;
import io.eventuate.util.test.async.Eventually;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OrdersAndCustomersInMemoryFailingCompensatingTransactionIntegrationTest.Config.class)
public class OrdersAndCustomersInMemoryFailingCompensatingTransactionIntegrationTest {

    @Configuration
    @Import(OrdersAndCustomersInMemoryIntegrationTestConfiguration.class)
    public static class Config {

        @Bean
        public OrderCommandHandler orderCommandHandler(OrderDao orderDao) {
            return new OrderCommandHandlerWithFailingCompensatingTransaction(orderDao);
        }

        @Bean
        public SagaLifecycleEventListener sagaStartedEventListener() {
            return new SagaLifecycleEventListener();
        }

    }

    @Autowired
    protected CustomerService customerService;

    @Autowired
    protected OrderService orderService;

    @Autowired
    private SagaLifecycleEventListener sagaEventListener;

    @Autowired
    private SagaInstanceRepository sagaInstanceRepository;

    @Autowired
    private CreateOrderSaga createOrderSaga;

    @Test
    public void shouldChangeStateOfSagaToFailedWhenCompensatingTransactionFails() {
        sagaEventListener.clear();

        createOrderThatExceedsCreateLimitAndStartSaga();

        String sagaId = getSagaId();

        assertSagaFailed(sagaId);

    }

    private void createOrderThatExceedsCreateLimitAndStartSaga() {
        Customer customer = customerService.createCustomer("Fred", new Money("15.00"));
        orderService.createOrder(new OrderDetails(customer.getId(), new Money("123.40")));
    }

    private String getSagaId() {
        SagaStartedEvent startedEvent = sagaEventListener.expectEvent(SagaStartedEvent.class);
        assertNotNull(startedEvent);
        return startedEvent.getSagaId();
    }

    private void assertSagaFailed(String sagaId) {
        String sagaType = createOrderSaga.getSagaType();
        Eventually.eventually(() -> {
            SagaInstance s = sagaInstanceRepository.find(sagaType, sagaId);
            assertTrue(s.isFailed());
        });

        sagaEventListener.expectEvent(SagaFailedEvent.class);

    }

}
