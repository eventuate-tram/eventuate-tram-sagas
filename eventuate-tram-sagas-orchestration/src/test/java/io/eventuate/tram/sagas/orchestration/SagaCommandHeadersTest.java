package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.sagas.common.SagaCommandHeaders;
import io.eventuate.tram.sagas.common.SagaReplyHeaders;
import org.junit.Test;

public class SagaCommandHeadersTest {

  @Test
  public void shouldDoSomething() {
    System.out.println("SAGA_TYPE=" + SagaCommandHeaders.SAGA_TYPE);
    System.out.println("REPLY_SAGA_TYPE=" + SagaReplyHeaders.REPLY_SAGA_TYPE);
  }

}