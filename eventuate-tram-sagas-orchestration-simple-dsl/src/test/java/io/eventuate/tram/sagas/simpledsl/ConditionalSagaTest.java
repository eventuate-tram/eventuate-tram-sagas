package io.eventuate.tram.sagas.simpledsl;

import org.junit.Test;

import static io.eventuate.tram.sagas.testing.SagaUnitTestSupport.given;

public class ConditionalSagaTest {

  @Test
  public void shouldExecuteAllStepsSuccessfully() {
    given().
       saga(new ConditionalSaga(), new ConditionalSagaData(true)).
    expect().
            command(new Do1Command()).
            to("participant1").
    andGiven().
      successReply().
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      successReply().
    expectCompletedSuccessfully()
    ;
  }

  @Test
  public void shouldRollback() {
    given().
       saga(new ConditionalSaga(), new ConditionalSagaData(true)).
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
  }

  @Test
  public void shouldExecuteAllStepsExcept1Successfully() {
    given().
       saga(new ConditionalSaga(), new ConditionalSagaData(false)).
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      successReply().
    expectCompletedSuccessfully()
    ;
  }

  @Test
  public void shouldRollbackExcept1() {
    given().
       saga(new ConditionalSaga(), new ConditionalSagaData(false)).
    expect().
      command(new ReserveCreditCommand()).
      to("participant2").
    andGiven().
      failureReply().
    expectRolledBack()
    ;
  }


  @Test
  public void shouldFailOnFirstStep() {
    given().
       saga(new ConditionalSaga(), new ConditionalSagaData(true)).
    expect().
       command(new Do1Command()).
       to("participant1").
    andGiven().
       failureReply().
       expectRolledBack()
    ;
  }

}
