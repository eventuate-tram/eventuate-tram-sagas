package io.eventuate.tram.sagas.orchestration;

import java.util.Collection;

public class SimpleSagaRegistrar implements SagaRegistrar {

    private final Collection<Saga> sagas;

    public SimpleSagaRegistrar(final Collection<Saga> sagas) {
        this.sagas = sagas;
    }

    @Override
    public Collection<Saga> getSagas() {
        return sagas;
    }
}
