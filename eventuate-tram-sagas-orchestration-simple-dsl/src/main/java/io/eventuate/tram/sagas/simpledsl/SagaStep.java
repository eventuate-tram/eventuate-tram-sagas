package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.messaging.common.Message;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface SagaStep<SAGA_DATA> extends AbstractSagaStep<SAGA_DATA> {
  Optional<BiConsumer<SAGA_DATA, Object>> getReplyHandler(Message message, boolean compensating);

  StepOutcome makeStepOutcome(SAGA_DATA sagaData, boolean compensating);
}
