package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.simpledsl.AbstractSagaActionsProvider;
import org.reactivestreams.Publisher;

import java.util.function.Supplier;

public class ReactiveSagaActionsProvider<Data> extends AbstractSagaActionsProvider<Data, Publisher<SagaActions<Data>>> {

    public ReactiveSagaActionsProvider(SagaActions<Data> sagaActions) {
        super(sagaActions);
    }

    public ReactiveSagaActionsProvider(Supplier<Publisher<SagaActions<Data>>> sagaActionsSupplier) {
        super(sagaActionsSupplier);
    }

}
