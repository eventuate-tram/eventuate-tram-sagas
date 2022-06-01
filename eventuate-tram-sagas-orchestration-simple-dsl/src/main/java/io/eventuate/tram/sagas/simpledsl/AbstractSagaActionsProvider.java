package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

public abstract class AbstractSagaActionsProvider<Data> {
    protected final SagaActions<Data> sagaActions;

    public AbstractSagaActionsProvider(SagaActions<Data> sagaActions) {
        this.sagaActions = sagaActions;
    }

    public SagaActions<Data> getSagaActions() {
        return sagaActions;
    }
}
