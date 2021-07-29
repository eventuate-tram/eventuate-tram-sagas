package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;

import java.util.LinkedList;
import java.util.List;

public class SimpleReactiveSagaDefinitionBuilder<Data> {

  private List<ReactiveSagaStep<Data>> sagaSteps = new LinkedList<>();

  public void addStep(ReactiveSagaStep<Data> sagaStep) {
    sagaSteps.add(sagaStep);
  }

  public ReactiveSagaDefinition<Data> build() {
    return new SimpleReactiveSagaDefinition<>(sagaSteps);
  }
}
