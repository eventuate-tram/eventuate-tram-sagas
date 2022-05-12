package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.Optional;
import java.util.function.Consumer;

public class LocalStepBuilder<SAGA_DATA>  {
  private final SimpleSagaDefinitionBuilder<SAGA_DATA> parent;
  private final Consumer<SAGA_DATA> localFunction;
  private Optional<Consumer<SAGA_DATA>> compensation = Optional.empty();

  public LocalStepBuilder(SimpleSagaDefinitionBuilder<SAGA_DATA> parent, Consumer<SAGA_DATA> localFunction) {
    this.parent = parent;
    this.localFunction = localFunction;
  }

  public LocalStepBuilder<SAGA_DATA> withCompensation(Consumer<SAGA_DATA> localCompensation) {
    this.compensation = Optional.of(localCompensation);
     return this;
  }


  public StepBuilder<SAGA_DATA> step() {
    parent.addStep(new LocalStep<>(localFunction, compensation));
    return new StepBuilder<>(parent);
  }

  public SagaDefinition<SagaActions<SAGA_DATA>, SAGA_DATA> build() {
    // TODO - pull up with template method for completing current step
    parent.addStep(new LocalStep<>(localFunction, compensation));
    return parent.build();
  }

}
