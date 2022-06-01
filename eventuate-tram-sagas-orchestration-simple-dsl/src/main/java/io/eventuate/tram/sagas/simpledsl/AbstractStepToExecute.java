package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

import static io.eventuate.tram.sagas.simpledsl.SagaExecutionStateJsonSerde.encodeState;

public class AbstractStepToExecute<Data, SagaStep extends ISagaStep<Data>> {
    protected final SagaStep step;
    protected final int skipped;
    protected final boolean compensating;

    public AbstractStepToExecute(SagaStep step, int skipped, boolean compensating) {
        this.step = step;
        this.skipped = skipped;
        this.compensating = compensating;
    }

    protected int size() {
      return 1 + skipped;
    }

    protected SagaActions<Data> makeSagaActions(SagaActions.Builder<Data> builder, Data data, SagaExecutionState newState, boolean compensating) {
        String state = encodeState(newState);
        return builder.buildActions(data, compensating, state, newState.isEndState());
    }



}
