package io.eventuate.tram.sagas.orchestration;

public interface SagaStateMachineAction<Data, Reply> {

  SagaActions<Data> apply(Data data, Reply reply);


}
