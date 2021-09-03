package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.messaging.common.Message;

public interface AbstractSagaStep<SAGA_DATA> {
  boolean isSuccessfulReply(boolean compensating, Message message);

  boolean hasAction(SAGA_DATA data);

  boolean hasCompensation(SAGA_DATA data);
}
