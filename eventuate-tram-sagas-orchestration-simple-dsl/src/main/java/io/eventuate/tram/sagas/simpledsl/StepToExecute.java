package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

public class StepToExecute<Data> extends AbstractStepToExecute<Data, SagaStep<Data>> {


  public StepToExecute(SagaStep<Data> step, int skipped, boolean compensating) {
    super(step, skipped, compensating);
  }


  public SagaActions<Data> executeStep(Data data, SagaExecutionState currentState) {
    SagaExecutionState newState = currentState.nextState(size());
    SagaActions.Builder<Data> builder = SagaActions.builder();
    boolean compensating = currentState.isCompensating();

    step.makeStepOutcome(data, this.compensating).visit(builder::withIsLocal, builder::withCommands);

    return makeSagaActions(builder, data, newState, compensating);
  }


}
