package io.eventuate.tram.sagas.orchestration;


public interface Saga<Data> {

  SagaDefinition<Data> getSagaDefinition();

  default String getSagaType() {
    return getClass().getName();
  }
}
