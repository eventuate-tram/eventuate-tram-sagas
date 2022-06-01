package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

import java.util.function.Supplier;

public class SagaActionsProvider<Data> extends AbstractSagaActionsProvider<Data> {

    private final Supplier<SagaActions<Data>> sagaActionsFunction;

    public SagaActionsProvider(SagaActions<Data> sagaActions) {
        super(sagaActions);
        sagaActionsFunction = null;
    }

    public SagaActionsProvider(Supplier<SagaActions<Data>> sagaActionsFunction) {
        super(null);
        this.sagaActionsFunction = sagaActionsFunction;
    }

    public Supplier<SagaActions<Data>> getSagaActionsFunction() {
        return sagaActionsFunction;
    }
}
