package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractSagaActionsProvider<Data, SuppliedValue> {
    private final SagaActions<Data> sagaActions;
    private final Supplier<SuppliedValue> sagaActionsSupplier;

    public AbstractSagaActionsProvider(SagaActions<Data> sagaActions) {
        this.sagaActions = sagaActions;
        this.sagaActionsSupplier = null;
    }

    public AbstractSagaActionsProvider(Supplier<SuppliedValue> sagaActionsSupplier) {
        this.sagaActions = null;
        this.sagaActionsSupplier = sagaActionsSupplier;
    }

    public SuppliedValue toSagaActions(Function<SagaActions<Data>, SuppliedValue> f1, Function<SuppliedValue, SuppliedValue> f2) {
        return sagaActions != null ? f1.apply(sagaActions) : f2.apply(sagaActionsSupplier.get());
    }

}
