package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.function.Function;

public class ReactiveLocalStepBuilder<SAGA_DATA>  {
  private final SimpleReactiveSagaDefinitionBuilder<SAGA_DATA> parent;
  private final Function<SAGA_DATA, Publisher<?>> localFunction;
  private Optional<Function<SAGA_DATA, Publisher<?>>> compensation = Optional.empty();

  public ReactiveLocalStepBuilder(SimpleReactiveSagaDefinitionBuilder<SAGA_DATA> parent, Function<SAGA_DATA, Publisher<?>> localFunction) {
    this.parent = parent;
    this.localFunction = localFunction;
  }

  public ReactiveLocalStepBuilder<SAGA_DATA> withCompensation(Function<SAGA_DATA, Publisher<?>> localCompensation) {
    this.compensation = Optional.of(localCompensation);
     return this;
  }


  public ReactiveStepBuilder<SAGA_DATA> step() {
    parent.addStep(new ReactiveLocalStep<>(localFunction, compensation));
    return new ReactiveStepBuilder<>(parent);
  }

  public SagaDefinition<Publisher<SagaActions<SAGA_DATA>>, SAGA_DATA> build() {
    parent.addStep(new ReactiveLocalStep<>(localFunction, compensation));
    return parent.build();
  }

}
