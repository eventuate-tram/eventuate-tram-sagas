package io.eventuate.tram.sagas.orchestration;


public interface Saga<Data> {

  SagaDefinition<Data> getSagaDefinition();

  default String getSagaType() {
    return getClass().getName();
  }

  default void onSagaCompletedSuccessfully(String sagaId, Data data) {  }
  default void onSagaRolledBack(String sagaId, Data data) {  }

}
