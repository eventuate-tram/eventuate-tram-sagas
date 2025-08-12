package io.eventuate.tram.sagas.simpledsl.notifications;

import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class NotificationBasedCreateOrderSagaTest {

  private NotificationBasedCreateOrderSagaSteps steps;

  @BeforeEach
  public void setUp() throws Exception {
    steps = mock(NotificationBasedCreateOrderSagaSteps.class);
  }

  @Test
  public void shouldExecuteAllStepsSuccessfully() {
    given().
       saga(new NotificationBasedCreateOrderSaga(steps), new NotificationBasedCreateOrderSagaData()).
    expect().
      command(new ReserveCreditCommand()).
      to("customerService").
    andGiven().
      successReply().
    expect().
      command(new ReserveInventory()).
      to("inventoryService").
    andGiven().
            successReply().
    expect().
            notification(new FulfillOrder()).
            to("fulfillmentService").
    expectCompletedSuccessfully()
    ;
  }

  @Test
  public void shouldRollbackFromStep2() {
    given().
       saga(new NotificationBasedCreateOrderSaga(steps), new NotificationBasedCreateOrderSagaData()).
    expect().
      command(new ReserveCreditCommand()).
      to("customerService").
    andGiven().
      failureReply().
    andGiven().
      expectRolledBack()
    ;
  }

  @Test
  public void shouldHandleFailureOfFirstLocalStep() {
    NotificationBasedCreateOrderSagaData data = new NotificationBasedCreateOrderSagaData();
    RuntimeException expectedCreateException = new RuntimeException("Failed local step");
    doThrow(expectedCreateException).when(steps).createOrder(data);
    given().
        saga(new NotificationBasedCreateOrderSaga(steps), data).
            expectException(expectedCreateException)
    ;
  }
  @Test
  public void shouldHandleFailureOfLastLocalStep() {
    doThrow(new RuntimeException()).when(steps).approveOrder(any());
    given().
            saga(new NotificationBasedCreateOrderSaga(steps), new NotificationBasedCreateOrderSagaData()).
            expect().
              command(new ReserveCreditCommand()).
              to("customerService").
            andGiven().
              successReply().
            expect().
              command(new ReserveInventory()).
              to("inventoryService").
            andGiven().
              successReply().
            expect().
              multiple().
                notification(new ReleaseInventory()).
                to("inventoryService").
                command(new ReleaseCreditCommand()).
                to("customerService").
                verify().
            andGiven().
              successReply().
            expectRolledBack()
    ;
  }


}
