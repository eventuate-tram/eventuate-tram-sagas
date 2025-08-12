package io.eventuate.tram.sagas.simpledsl.localexceptions;

import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;
import io.eventuate.tram.sagas.simpledsl.notifications.FulfillOrder;
import io.eventuate.tram.sagas.simpledsl.notifications.ReleaseInventory;
import io.eventuate.tram.sagas.simpledsl.notifications.ReserveInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
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

  static class UnexpectedException extends RuntimeException {
  }

  @Test
  public void shouldFailWithUnexpectedException() {
    assertThrows(UnexpectedException.class, () -> {
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
    });
  }

}