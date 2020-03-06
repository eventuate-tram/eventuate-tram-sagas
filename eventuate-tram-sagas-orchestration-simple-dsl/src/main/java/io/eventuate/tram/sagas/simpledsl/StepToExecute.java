package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

import java.util.Optional;

import static io.eventuate.tram.sagas.simpledsl.SagaExecutionStateJsonSerde.encodeState;

public class StepToExecute<Data> {
  private final Optional<SagaStep<Data>> step;
  private final int skipped;
  private final boolean compensating;


  public StepToExecute(Optional<SagaStep<Data>> step, int skipped, boolean compensating) {
    this.compensating = compensating;
    this.step = step;
    this.skipped = skipped;
  }


  private int size() {
    return step.map(x -> 1).orElse(0) + skipped;
  }

  public boolean isEmpty() {
    return !step.isPresent();
  }

  public SagaActions<Data> executeStep(Data data, SagaExecutionState currentState) {
    SagaExecutionState newState = currentState.nextState(size());
    SagaActions.Builder<Data> builder = SagaActions.builder();
    boolean compensating = currentState.isCompensating();

    step.get().makeStepOutcome(data, this.compensating).visit(builder::withIsLocal, builder::withCommands);

    return builder
            .withUpdatedSagaData(data)
            .withUpdatedState(encodeState(newState))
            .withIsEndState(newState.isEndState())
            .withIsCompensating(compensating)
            .build();
  }

}
