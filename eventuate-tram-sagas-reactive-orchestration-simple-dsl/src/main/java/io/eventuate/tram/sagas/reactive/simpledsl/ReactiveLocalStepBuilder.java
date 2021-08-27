package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.function.Function;

public class ReactiveLocalStepBuilder<Data>  {
  private final SimpleReactiveSagaDefinitionBuilder<Data> parent;
  private final Function<Data, Publisher<?>> localFunction;
  private Optional<Function<Data, Publisher<?>>> compensation = Optional.empty();

  public ReactiveLocalStepBuilder(SimpleReactiveSagaDefinitionBuilder<Data> parent, Function<Data, Publisher<?>> localFunction) {
    this.parent = parent;
    this.localFunction = localFunction;
  }

  public ReactiveLocalStepBuilder<Data> withCompensation(Function<Data, Publisher<?>> localCompensation) {
    this.compensation = Optional.of(localCompensation);
     return this;
  }


  public ReactiveStepBuilder<Data> step() {
    parent.addStep(new ReactiveLocalStep<>(localFunction, compensation));
    return new ReactiveStepBuilder<>(parent);
  }

  public ReactiveSagaDefinition<Data> build() {
    parent.addStep(new ReactiveLocalStep<>(localFunction, compensation));
    return parent.build();
  }

}
