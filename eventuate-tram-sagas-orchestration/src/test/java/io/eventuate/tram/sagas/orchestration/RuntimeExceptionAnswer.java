package io.eventuate.tram.sagas.orchestration;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class RuntimeExceptionAnswer implements Answer<Object> {

  public static final RuntimeExceptionAnswer INSTANCE = new RuntimeExceptionAnswer();

  @Override
  public Object answer(InvocationOnMock invocation) throws Throwable {
    throw new RuntimeException();
  }
}
