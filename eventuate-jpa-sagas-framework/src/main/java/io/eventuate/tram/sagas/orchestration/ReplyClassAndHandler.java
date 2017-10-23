package io.eventuate.tram.sagas.orchestration;

public interface ReplyClassAndHandler {
  RawSagaStateMachineAction getReplyHandler();

  Class<?> getReplyClass();
}
