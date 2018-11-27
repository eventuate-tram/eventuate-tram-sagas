package io.eventuate.tram.sagas.orchestration;

import java.util.function.Function;

public interface StartingHandler<Data> extends Function<Data, SagaActions> {
}
