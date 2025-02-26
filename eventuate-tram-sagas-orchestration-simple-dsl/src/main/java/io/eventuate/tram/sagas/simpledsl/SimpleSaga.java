package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.Saga;

import java.util.Collections;
import java.util.List;

public interface SimpleSaga<Data> extends Saga<Data>, SimpleSagaDsl<Data> {
  default List<Object> getParticipantProxies() { return Collections.emptyList(); }
}
