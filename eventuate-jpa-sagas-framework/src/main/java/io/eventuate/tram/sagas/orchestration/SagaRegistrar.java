package io.eventuate.tram.sagas.orchestration;

import java.util.Collection;

public interface SagaRegistrar {
    Collection<Saga> getSagas();
}
