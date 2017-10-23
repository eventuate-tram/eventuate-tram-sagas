package io.eventuate.tram.sagas.orchestration;

public interface RawSagaStateMachineAction {

  SagaActions apply(Object sagaData, Object reply);
}
