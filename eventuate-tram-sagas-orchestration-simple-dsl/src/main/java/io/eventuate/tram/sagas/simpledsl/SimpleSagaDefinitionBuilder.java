package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.LinkedList;
import java.util.List;

public class SimpleSagaDefinitionBuilder<SAGA_DATA> {

  private List<SagaStep<SAGA_DATA>> sagaSteps = new LinkedList<>();

  public void addStep(SagaStep<SAGA_DATA> sagaStep) {
    sagaSteps.add(sagaStep);
  }

  public SagaDefinition<SagaActions<SAGA_DATA>, SAGA_DATA> build() {
    return new SimpleSagaDefinition<>(sagaSteps);
  }
}
