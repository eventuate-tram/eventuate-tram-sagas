package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.simpledsl.StepOutcome;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ReactiveLocalStep<Data> implements ReactiveSagaStep<Data> {
  private Function<Data, Publisher<?>> localFunction;
  private Optional<Function<Data, Publisher<?>>> compensation;

  public ReactiveLocalStep(Function<Data, Publisher<?>> localFunction, Optional<Function<Data, Publisher<?>>> compensation) {
    this.localFunction = localFunction;
    this.compensation = compensation;
  }

  @Override
  public boolean hasAction(Data data) {
    return true;
  }

  @Override
  public boolean hasCompensation(Data data) {
    return compensation.isPresent();
  }


  @Override
  public boolean isSuccessfulReply(boolean compensating, Message message) {
    return CommandReplyOutcome.SUCCESS.name().equals(message.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME));
  }

  @Override
  public Optional<BiFunction<Data, Object, Publisher<?>>> getReplyHandler(Message message, boolean compensating) {
    return Optional.empty();
  }

  @Override
  public Publisher<StepOutcome> makeStepOutcome(Data data, boolean compensating) {

    Publisher<?> result;

    if (compensating) {
      result = compensation.map(localStep -> localStep.apply(data)).orElse(Mono.empty());
    } else {
      result =  localFunction.apply(data);
    }

    return Mono
            .from(result)
            .then(Mono.defer(() -> Mono.just(StepOutcome.makeLocalOutcome(Optional.empty()))))
            .onErrorResume(RuntimeException.class, e -> Mono.just(StepOutcome.makeLocalOutcome(Optional.of(e))));
  }

}
