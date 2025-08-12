package io.eventuate.tram.sagas.reactive.simpledsl;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static io.eventuate.tram.sagas.reactive.simpledsl.framework.ReactiveSagaUnitTestSupport.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class ReactiveLocalSagaTest extends AbstractReactiveLocalSagaTest {

  @Override
  protected SimpleReactiveSaga<LocalSagaData> makeSaga() {
    return new ReactiveLocalSaga(steps);
  }


  @Test
  public void shouldExecuteAllStepsSuccessfully() {
      doReturn(Mono.empty()).when(steps).localStep1(any());
      doReturn(Mono.empty()).when(steps).localStep1Compensation(any());
      doReturn(Mono.empty()).when(steps).localStep3(any());

      given().
              saga(makeSaga(), new LocalSagaData()).
              expect().
              command(new ReserveCreditCommand()).
              to("participant2").
              andGiven().
              successReply()
              .expectCompletedSuccessfully()
      ;

      verify(steps).localStep3(any());
  }

  @Test
  public void shouldHandleFailureOfLastLocalStep() {
      doReturn(Mono.empty()).when(steps).localStep1(any());
      doReturn(Mono.empty()).when(steps).localStep1Compensation(any());
      doReturn(Mono.error(new RuntimeException())).when(steps).localStep3(any());

      given().
              saga(makeSaga(), new LocalSagaData()).
              expect().
              command(new ReserveCreditCommand()).
              to("participant2").
              andGiven().
              successReply().
              expect().
              command(new ReleaseCreditCommand()).
              to("participant2").
              andGiven().
              successReply().
              expectRolledBack()
      ;
  }
}
