package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import static org.junit.Assert.*;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ApproveOrderCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.RejectOrderCommand;
import org.junit.Test;

public class CreateOrderSagaTest {

  private long orderId = 101;
  private long customerId = 102;
  private Money orderTotal = new Money("12.34");
  private OrderDetails orderDetails = new OrderDetails(customerId, orderTotal);

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
     .withPublishedEventOfType(CreateOrderSagaCompletedSuccesfully.class)
        ;

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
       .withPublishedEventOfType(CreateOrderSagaRolledBack.class)
        ;

  }

  private CreateOrderSaga makeCreateOrderSaga() {
    return new CreateOrderSaga();
  }
}