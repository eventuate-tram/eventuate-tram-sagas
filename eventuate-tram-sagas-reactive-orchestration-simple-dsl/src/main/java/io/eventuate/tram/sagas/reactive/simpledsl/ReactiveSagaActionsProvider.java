package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.simpledsl.AbstractSagaActionsProvider;
import org.reactivestreams.Publisher;

import java.util.function.Supplier;

public class ReactiveSagaActionsProvider<Data> extends AbstractSagaActionsProvider<Data> {

    private final Supplier<Publisher<SagaActions<Data>>> sagaActionsFunction;

    public ReactiveSagaActionsProvider(SagaActions<Data> sagaActions) {
        super(sagaActions);
        this.sagaActionsFunction = null;
    }

    public ReactiveSagaActionsProvider(Supplier<Publisher<SagaActions<Data>>> sagaActionsFunction) {
        super(null);
        this.sagaActionsFunction = sagaActionsFunction;
    }

    public Supplier<Publisher<SagaActions<Data>>> getSagaActionsFunction() {
        return sagaActionsFunction;
    }
}
