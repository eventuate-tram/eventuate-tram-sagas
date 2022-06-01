package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.simpledsl.ISagaStep;
import io.eventuate.tram.sagas.simpledsl.StepOutcome;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.function.BiFunction;

public interface ReactiveSagaStep<Data> extends ISagaStep<Data>  {
  Optional<BiFunction<Data, Object, Publisher<?>>> getReplyHandler(Message message, boolean compensating);

  Publisher<StepOutcome>  makeStepOutcome(Data data, boolean compensating);
}
