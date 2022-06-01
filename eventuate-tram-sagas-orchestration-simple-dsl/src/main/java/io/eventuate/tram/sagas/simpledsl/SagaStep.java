package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.messaging.common.Message;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface SagaStep<Data> extends ISagaStep<Data> {

  Optional<BiConsumer<Data, Object>> getReplyHandler(Message message, boolean compensating);

  StepOutcome makeStepOutcome(Data data, boolean compensating);

}
