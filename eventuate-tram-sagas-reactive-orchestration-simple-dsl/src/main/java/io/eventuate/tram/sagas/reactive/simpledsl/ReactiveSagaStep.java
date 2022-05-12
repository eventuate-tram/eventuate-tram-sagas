package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.simpledsl.AbstractSagaStep;
import io.eventuate.tram.sagas.simpledsl.StepOutcome;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.function.BiFunction;

public interface ReactiveSagaStep<SAGA_DATA> extends AbstractSagaStep<SAGA_DATA> {
  Optional<BiFunction<SAGA_DATA, Object, Publisher<?>>> getReplyHandler(Message message, boolean compensating);

  Publisher<StepOutcome>  makeStepOutcome(SAGA_DATA data, boolean compensating);
}
