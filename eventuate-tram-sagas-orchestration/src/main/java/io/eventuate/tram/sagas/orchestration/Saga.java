package io.eventuate.tram.sagas.orchestration;


public interface Saga<DATA> {

  SagaDefinition<SagaActions<DATA>, DATA> getSagaDefinition();

  default String getSagaType() {
    return getClass().getName().replace("$", "_DLR_");
  }

  default void onStarting(String sagaId, DATA data) {  }
  default void onSagaCompletedSuccessfully(String sagaId, DATA data) {  }
  default void onSagaRolledBack(String sagaId, DATA data) {  }

}
