package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSaga;

public interface SimpleReactiveSaga<Data> extends ReactiveSaga<Data>, SimpleReactiveSagaDsl<Data> {
}
