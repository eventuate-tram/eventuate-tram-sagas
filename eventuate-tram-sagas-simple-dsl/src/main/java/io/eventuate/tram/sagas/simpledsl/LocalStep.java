package io.eventuate.tram.sagas.simpledsl;

import java.util.Optional;
import java.util.function.Consumer;

public class LocalStep<Data> implements SagaStep<Data> {
  private Consumer<Data> localFunction;
  private Optional<Consumer<Data>> compensation;

  public LocalStep(Consumer<Data> localFunction, Optional<Consumer<Data>> compensation) {
    this.localFunction = localFunction;
    this.compensation = compensation;
  }

  public void execute(Data data, boolean compensating) {
    if (compensating)
      compensation.ifPresent(c -> c.accept(data));
    else
      localFunction.accept(data);
  }
}
