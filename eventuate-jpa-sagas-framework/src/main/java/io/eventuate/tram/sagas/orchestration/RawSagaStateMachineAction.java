package io.eventuate.tram.sagas.orchestration;

@FunctionalInterface
public interface RawSagaStateMachineAction<Data> {
    SagaActions<Data> apply(final Data sagaData, final Object reply);
}
