package io.eventuate.tram.sagas.simpledsl.notifications;

import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;
import org.junit.Before;
import org.junit.Test;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ConditionalNotificationBasedCreateOrderSagaTest {

  private ConditionalNotificationBasedCreateOrderSagaSteps steps;

  @Before
  public void setUp() throws Exception {
    steps = mock(ConditionalNotificationBasedCreateOrderSagaSteps.class);
  }

  @Test
  public void shouldExecuteAllStepsSuccessfully() {
    given().
       saga(new ConditionalNotificationBasedCreateOrderSaga(steps), new ConditionalNotificationBasedCreateOrderSagaData()).
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
  public void shouldExecuteAllStepsSuccessfullyWithSkippedFulfilOrder() {
    given().
       saga(new ConditionalNotificationBasedCreateOrderSaga(steps),
               new ConditionalNotificationBasedCreateOrderSagaData().skipFulfillOrder()).
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
    expectCompletedSuccessfully()
    ;
  }

  @Test
  public void shouldHandleFailureOfLastLocalStep() {
    doThrow(new RuntimeException()).when(steps).approveOrder(any());
    given().
            saga(new ConditionalNotificationBasedCreateOrderSaga(steps), new ConditionalNotificationBasedCreateOrderSagaData()).
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


  @Test
  public void shouldHandleFailureOfLastLocalStepWithSkippedReleaseInventory() {
    doThrow(new RuntimeException()).when(steps).approveOrder(any());
    given().
            saga(new ConditionalNotificationBasedCreateOrderSaga(steps),
                    new ConditionalNotificationBasedCreateOrderSagaData()
                            .skipReleaseInventory()).
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
                command(new ReleaseCreditCommand()).
                to("customerService").
                verify().
            andGiven().
              successReply().
            expectRolledBack()
    ;
  }


}
