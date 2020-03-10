package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.Optional;
import java.util.function.Consumer;

public class LocalStepBuilder<Data>  {
  private final SimpleSagaDefinitionBuilder<Data> parent;
  private final Consumer<Data> localFunction;
  private Optional<Consumer<Data>> compensation = Optional.empty();

  public LocalStepBuilder(SimpleSagaDefinitionBuilder<Data> parent, Consumer<Data> localFunction) {
    this.parent = parent;
    this.localFunction = localFunction;
  }

  public LocalStepBuilder<Data> withCompensation(Consumer<Data> localCompensation) {
    this.compensation = Optional.of(localCompensation);
     return this;
  }


  public StepBuilder<Data> step() {
    parent.addStep(new LocalStep<>(localFunction, compensation));
    return new StepBuilder<>(parent);
  }

  public SagaDefinition<Data> build() {
    // TODO - pull up with template method for completing current step
    parent.addStep(new LocalStep<>(localFunction, compensation));
    return parent.build();
  }

}
