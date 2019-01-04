package io.eventuate.tram.sagas.simpledsl;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class StepCoordinatorImpl<Data> implements StepCoordinator<Data> {

    private final List<SagaStep<Data>> sagaSteps;

    public StepCoordinatorImpl(final List<SagaStep<Data>> sagaSteps) {
        this.sagaSteps = sagaSteps;
    }

    @Override
    public StepsToExecute<Data> nextStepsToExecute(final SagaExecutionState state) {
        List<LocalStep<Data>> localSteps = new LinkedList<>();

        int skipped = 0;
        if (state.isCompensating()) {
            int i = state.getCurrentlyExecuting() - 1;
            while (i >= 0) {
                SagaStep<Data> step = sagaSteps.get(i--);
                if (step instanceof LocalStep) {
                    localSteps.add((LocalStep<Data>) step);
                } else if (step instanceof ParticipantInvocationStep && ((ParticipantInvocationStep) step).hasCompensation()) {
                    return new StepsToExecute<>(localSteps, Optional.of((ParticipantInvocationStep) step), skipped);
                } else
                    skipped++;
            }
            return new StepsToExecute<>(localSteps, Optional.empty(), skipped);

        } else {
            int i = state.getCurrentlyExecuting() + 1;
            while (i < sagaSteps.size()) {
                SagaStep<Data> step = sagaSteps.get(i++);
                if (step instanceof LocalStep) {
                    localSteps.add((LocalStep<Data>) step);
                } else if (step instanceof ParticipantInvocationStep && ((ParticipantInvocationStep) step).hasAction()) {
                    return new StepsToExecute<>(localSteps, Optional.of((ParticipantInvocationStep) step), skipped);
                } else
                    skipped++;
            }
            return new StepsToExecute<>(localSteps, Optional.empty(), skipped);
        }
    }

    @Override
    public ParticipantInvocationStep<Data> participantInvocationStepFor(final SagaExecutionState state) {
        return (ParticipantInvocationStep<Data>) sagaSteps.get(state.getCurrentlyExecuting());
    }

}
