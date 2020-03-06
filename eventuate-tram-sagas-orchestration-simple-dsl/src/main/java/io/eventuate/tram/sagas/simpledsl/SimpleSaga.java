package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.Saga;

public interface SimpleSaga<Data> extends Saga<Data>, SimpleSagaDsl<Data> {
}
