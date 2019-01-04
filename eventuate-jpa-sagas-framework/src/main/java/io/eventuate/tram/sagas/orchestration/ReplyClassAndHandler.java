package io.eventuate.tram.sagas.orchestration;

public interface ReplyClassAndHandler<Data> {
  RawSagaStateMachineAction<Data> getReplyHandler();

  Class<?> getReplyClass();
}
