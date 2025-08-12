package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ApproveOrderCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.RejectOrderCommand;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.sagas.testing.SagaUnitTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class CreateOrderSagaTest {

  private long orderId = 101;
  private long customerId = 102;
  private Money orderTotal = new Money("12.34");
  private OrderDetails orderDetails = new OrderDetails(customerId, orderTotal);

  @Mock
  private DomainEventPublisher domainEventPublisher;

  @Test
  public void shouldCreateOrder() {
    given()
      .saga(makeCreateOrderSaga(), new CreateOrderSagaData(orderId, orderDetails))
    .expect()
      .command(new ReserveCreditCommand(customerId, orderId, orderTotal))
      .to("customerService")
    .andGiven()
            .successReply()
    .expect()
        .command(new ApproveOrderCommand(orderId))
        .to("orderService")
    .andGiven()
        .successReply()
    .expectCompletedSuccessfully()
        ;

    verify(domainEventPublisher).publish(eq(CreateOrderSaga.class), eq(SagaUnitTestSupport.SAGA_ID), argThat(events -> {
      assertEquals(1, events.size());
      CreateOrderSagaCompletedSuccesfully event = (CreateOrderSagaCompletedSuccesfully) events.get(0);
      assertEquals(orderId, event.getOrderId());
      return true;
    }));
  }

  @Test
  public void shouldRejectOrder() {
    given()
      .saga(makeCreateOrderSaga(), new CreateOrderSagaData(orderId, orderDetails))
    .expect()
       .command(new ReserveCreditCommand(customerId, orderId, orderTotal))
       .to("customerService")
    .andGiven()
       .failureReply()
    .expect()
       .command(new RejectOrderCommand(orderId))
       .to("orderService")
    .andGiven()
       .successReply()
    .expectRolledBack()
        ;


    verify(domainEventPublisher).publish(eq(CreateOrderSaga.class), eq(SagaUnitTestSupport.SAGA_ID), argThat(events -> {
      assertEquals(1, events.size());
      CreateOrderSagaRolledBack event = (CreateOrderSagaRolledBack) events.get(0);
      assertEquals(orderId, event.getOrderId());
      return true;
    }));


  }

  private CreateOrderSaga makeCreateOrderSaga() {
    return new CreateOrderSaga(domainEventPublisher);
  }
}