package io.eventuate.tram.sagas.simpledsl;

import org.junit.Before;
import org.junit.Test;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class LocalSagaTest {

  private LocalSagaSteps steps;

  @Before
  public void setUp() throws Exception {
    steps = mock(LocalSagaSteps.class);
  }

  @Test
  public void shouldExecuteAllStepsSuccessfully() {
    given().
       saga(new LocalSaga(steps), new LocalSagaData()).
    expect().
      command(new Do2Command()).
      to("participant2").
    andGiven().
      successReply().
    expectCompletedSuccessfully()
    ;
  }

  @Test
  public void shouldRollbackFromStep2() {
    given().
       saga(new LocalSaga(steps), new LocalSagaData()).
    expect().
      command(new Do2Command()).
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
        saga(new LocalSaga(steps), data).
            expectException(expectedCreateException)
    ;
  }
  @Test
  public void shouldHandleFailureOfLastLocalStep() {
    LocalSagaData data = new LocalSagaData();
    doThrow(new RuntimeException()).when(steps).localStep3(data);
    given().
            saga(new LocalSaga(steps), data).
            expect().
            command(new Do2Command()).
            to("participant2").
            andGiven().
            successReply().
            expect().
            command(new Undo2Command()).
            to("participant2").
            expectRolledBack()
    ;
  }


}
