package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

import java.util.Optional;

import static io.eventuate.tram.sagas.simpledsl.SagaExecutionStateJsonSerde.encodeState;

public class StepToExecute<SAGA_DATA> extends AbstractStepToExecute<SagaStep<SAGA_DATA>, SagaActions<SAGA_DATA>, SAGA_DATA> {
  public StepToExecute(Optional<SagaStep<SAGA_DATA>> step, int skipped, boolean compensating) {
    super(step, skipped, compensating);
  }

  @Override
  public SagaActions<SAGA_DATA> executeStep(SAGA_DATA data, SagaExecutionState currentState) {
    SagaExecutionState newState = currentState.nextState(size());
    SagaActions.Builder<SAGA_DATA> builder = SagaActions.builder();
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
