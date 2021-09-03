package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import org.reactivestreams.Publisher;

import java.util.LinkedList;
import java.util.List;

public class SimpleReactiveSagaDefinitionBuilder<SAGA_DATA> {

  private List<ReactiveSagaStep<SAGA_DATA>> sagaSteps = new LinkedList<>();

  public void addStep(ReactiveSagaStep<SAGA_DATA> sagaStep) {
    sagaSteps.add(sagaStep);
  }

  public SagaDefinition<Publisher<SagaActions<SAGA_DATA>>, SAGA_DATA> build() {
    return new SimpleReactiveSagaDefinition<>(sagaSteps);
  }
}
