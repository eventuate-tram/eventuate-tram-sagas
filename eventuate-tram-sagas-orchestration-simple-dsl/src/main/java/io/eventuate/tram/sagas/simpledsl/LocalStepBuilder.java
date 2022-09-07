package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LocalStepBuilder<Data>  {
  private final SimpleSagaDefinitionBuilder<Data> parent;
  private final Consumer<Data> localFunction;
  private Optional<Consumer<Data>> compensation = Optional.empty();

  private final List<LocalExceptionSaver<Data>> localExceptionSavers = new LinkedList<>();
  private final List<Class<RuntimeException>> rollbackExceptions = new LinkedList<>();

  public LocalStepBuilder(SimpleSagaDefinitionBuilder<Data> parent, Consumer<Data> localFunction) {
    this.parent = parent;
    this.localFunction = localFunction;
  }

  public LocalStepBuilder<Data> withCompensation(Consumer<Data> localCompensation) {
    this.compensation = Optional.of(localCompensation);
     return this;
  }


  public StepBuilder<Data> step() {
    parent.addStep(makeLocalStep());
    return new StepBuilder<>(parent);
  }

  private LocalStep<Data> makeLocalStep() {
    return new LocalStep<>(localFunction, compensation, localExceptionSavers, rollbackExceptions);
  }

  public SagaDefinition<Data> build() {
    // TODO - pull up with template method for completing current step
    parent.addStep(makeLocalStep());
    return parent.build();
  }

  public <E extends RuntimeException> LocalStepBuilder<Data> onException(Class<E> exceptionType, BiConsumer<Data, E> exceptionConsumer) {
      rollbackExceptions.add((Class<RuntimeException>)exceptionType);
      localExceptionSavers.add(new LocalExceptionSaver<>(exceptionType, (BiConsumer<Data,RuntimeException>)exceptionConsumer));
      return this;
  }

  public <E extends RuntimeException> LocalStepBuilder<Data> onExceptionRollback(Class<E> exceptionType) {
    rollbackExceptions.add((Class<RuntimeException>)exceptionType);
    return this;
  }
}
