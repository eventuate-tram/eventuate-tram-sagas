package io.eventuate.tram.sagas.simpledsl.localexceptions;

import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;
import io.eventuate.tram.sagas.simpledsl.notifications.FulfillOrder;
import io.eventuate.tram.sagas.simpledsl.notifications.ReleaseInventory;
import io.eventuate.tram.sagas.simpledsl.notifications.ReserveInventory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class LocalExceptionCreateOrderSagaTest {

  @Mock
  private LocalExceptionCreateOrderSagaSteps steps;

  @Test
  public void shouldExecuteAllStepsSuccessfully() {
    given().
            saga(new LocalExceptionCreateOrderSaga(steps), new LocalExceptionCreateOrderSagaData()).
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
  public void shouldRollbackDueToLocalStepFailure() {
    doThrow(new InvalidOrderException()).when(steps).approveOrder(any());
    given().
            saga(new LocalExceptionCreateOrderSaga(steps), new LocalExceptionCreateOrderSagaData()).
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
            .assertSagaData(sagaData -> assertTrue(sagaData.isInvalidOrder()))
    ;
  }

  static class UnexpectedException extends RuntimeException {}

  @Test(expected = UnexpectedException.class)
  public void shouldFailWithUnexpectedException() {
    doThrow(new UnexpectedException()).when(steps).approveOrder(any());
    given().
            saga(new LocalExceptionCreateOrderSaga(steps), new LocalExceptionCreateOrderSagaData()).
            expect().
            command(new ReserveCreditCommand()).
            to("customerService").
            andGiven().
            successReply().
            expect().
            command(new ReserveInventory()).
            to("inventoryService").
            andGiven().
            successReply();
  }

}