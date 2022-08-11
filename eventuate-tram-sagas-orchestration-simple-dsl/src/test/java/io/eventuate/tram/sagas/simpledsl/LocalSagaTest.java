package io.eventuate.tram.sagas.simpledsl;

import org.junit.Before;
import org.junit.Test;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class LocalSagaTest {

  private LocalSagaSteps steps;

  @Before
  public void setUp() {
    steps = mock(LocalSagaSteps.class);
  }

  @Test
  public void shouldExecuteAllStepsSuccessfully() {
    given().
       saga(makeSaga(), new LocalSagaData()).
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      successReply().
    expectCompletedSuccessfully()
    ;
  }

  @Test
  public void shouldRollbackFromStep2() {
    given().
       saga(makeSaga(), new LocalSagaData()).
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      failureReply().
    andGiven().
      expectRolledBack()
    ;
  }

  @Test
  public void shouldHandleFailureOfFirstLocalStep() {
    LocalSagaData data = new LocalSagaData();
    RuntimeException expectedCreateException = new RuntimeException("Failed local step");
    doThrow(expectedCreateException).when(steps).localStep1(data);
    given().
        saga(makeSaga(), data).
            expectException(expectedCreateException)
    ;
  }
  @Test
  public void shouldHandleFailureOfLastLocalStep() {
    doThrow(new RuntimeException()).when(steps).localStep3(any());
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

  private LocalSaga makeSaga() {
    return new LocalSaga(steps);
  }


}
