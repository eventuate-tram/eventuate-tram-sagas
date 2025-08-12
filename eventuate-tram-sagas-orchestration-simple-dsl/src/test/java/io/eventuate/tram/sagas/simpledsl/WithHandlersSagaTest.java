package io.eventuate.tram.sagas.simpledsl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class WithHandlersSagaTest {

  private Handlers handlers;

  @BeforeEach
  public void setup() {
    this.handlers = mock(Handlers.class);
  }

  @AfterEach
  public void tearDown() {
    verifyNoMoreInteractions(handlers);
  }

  @Test
  public void shouldExecuteAllStepsSuccessfully() {
    given().
       saga(new WithHandlersSaga(handlers), new ConditionalSagaData(true)).
    expect().
            command(new Do1Command()).
            to("participant1").
            withExtraHeaders(ConditionalSagaData.DO1_COMMAND_EXTRA_HEADERS).
    andGiven().
      successReply().
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      successReply().
    expectCompletedSuccessfully()
    ;

    expectSuccess1();
    expectSuccess2();

  }

  private void expectSuccess1() {
    Mockito.verify(handlers).success1(any(), any());
  }

  @Test
  public void shouldRollback() {
    given().
       saga(new WithHandlersSaga(handlers), new ConditionalSagaData(true)).
    expect().
            command(new Do1Command()).
            to("participant1").
    andGiven().
      successReply().
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      failureReply().
    expect().
      command(new Undo1Command()).
      to("participant1").
    andGiven().
      successReply().
      expectRolledBack()
    ;

    expectSuccess1();
    expectFailure2();
    Mockito.verify(handlers).compensating1(any(), any());

  }

  private void expectFailure2() {
    Mockito.verify(handlers).failure2(any(), any());
  }

  @Test
  public void shouldExecuteAllStepsExcept1Successfully() {
    given().
       saga(new WithHandlersSaga(handlers), new ConditionalSagaData(false)).
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      successReply().
    expectCompletedSuccessfully()
    ;

    expectSuccess2();

  }

  private void expectSuccess2() {
    Mockito.verify(handlers).success2(any(), any());
  }

  @Test
  public void shouldRollbackExcept1() {
    given().
       saga(new WithHandlersSaga(handlers), new ConditionalSagaData(false)).
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      failureReply().
    expectRolledBack()
    ;

    expectFailure2();
  }


  @Test
  public void shouldFailOnFirstStep() {
    given().
       saga(new WithHandlersSaga(handlers), new ConditionalSagaData(true)).
    expect().
       command(new Do1Command()).
       to("participant1").
    andGiven().
       failureReply().
       expectRolledBack()
    ;

    expectFailure1();

  }

  private void expectFailure1() {
    Mockito.verify(handlers).failure1(any(), any());
  }

}
