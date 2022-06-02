package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

import java.util.function.Supplier;

public class SagaActionsProvider<Data> extends AbstractSagaActionsProvider<Data, SagaActions<Data>> {

    public SagaActionsProvider(SagaActions<Data> sagaActions) {
        super(sagaActions);
    }

    public SagaActionsProvider(Supplier<SagaActions<Data>> sagaActionsSupport) {
        super(sagaActionsSupport);
    }

}
