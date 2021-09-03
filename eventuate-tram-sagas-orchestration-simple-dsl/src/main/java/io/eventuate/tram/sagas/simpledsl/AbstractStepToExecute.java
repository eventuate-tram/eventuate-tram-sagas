package io.eventuate.tram.sagas.simpledsl;

import java.util.Optional;

public abstract class AbstractStepToExecute<SAGA_STEP, SAGA_ACTIONS, DATA> {
  protected final Optional<SAGA_STEP> step;
  protected final int skipped;
  protected final boolean compensating;

  public AbstractStepToExecute(Optional<SAGA_STEP> step,
                               int skipped,
                               boolean compensating) {
    this.compensating = compensating;
    this.step = step;
    this.skipped = skipped;
  }


  protected int size() {
    return step.map(x -> 1).orElse(0) + skipped;
  }

  public boolean isEmpty() {
    return !step.isPresent();
  }

  public abstract SAGA_ACTIONS executeStep(DATA data, SagaExecutionState currentState);
}
