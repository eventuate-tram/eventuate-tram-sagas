package io.eventuate.tram.sagas.simpledsl;

public interface StepCoordinator<Data> {
    StepsToExecute<Data> nextStepsToExecute(final SagaExecutionState state);

    ParticipantInvocationStep<Data> participantInvocationStepFor(final SagaExecutionState state);
}
