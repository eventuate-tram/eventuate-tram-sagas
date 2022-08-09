package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;
import io.eventuate.tram.sagas.simpledsl.StepOutcome;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ReactiveParticipantInvocationStep<Data> implements ReactiveSagaStep<Data> {
  private final Map<String, BiFunction<Data, Object, Publisher<?>>> actionReplyHandlers;
  private final Map<String, BiFunction<Data, Object, Publisher<?>>> compensationReplyHandlers;
  private Optional<ReactiveParticipantInvocation<Data>> participantInvocation;
  private Optional<ReactiveParticipantInvocation<Data>> compensation;

  public ReactiveParticipantInvocationStep(Optional<ReactiveParticipantInvocation<Data>> participantInvocation,
                                           Optional<ReactiveParticipantInvocation<Data>> compensation,
                                           Map<String, BiFunction<Data, Object, Publisher<?>>> actionReplyHandlers,
                                           Map<String, BiFunction<Data, Object, Publisher<?>>> compensationReplyHandlers) {
    this.actionReplyHandlers = actionReplyHandlers;
    this.compensationReplyHandlers = compensationReplyHandlers;
    this.participantInvocation = participantInvocation;
    this.compensation = compensation;
  }

  private Optional<ReactiveParticipantInvocation<Data>> getParticipantInvocation(boolean compensating) {
    return compensating ? compensation : participantInvocation;
  }

  public boolean hasAction(Data data) {
    return participantInvocation.isPresent() && participantInvocation.map(p -> p.isInvocable(data)).orElse(true);
  }

  public boolean hasCompensation(Data data) {
    return compensation.isPresent() && compensation.map(p -> p.isInvocable(data)).orElse(true);
  }

  public Optional<BiFunction<Data, Object, Publisher<?>>> getReplyHandler(Message message, boolean compensating) {
    String replyType = message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE);
    return Optional.ofNullable((compensating ? compensationReplyHandlers : actionReplyHandlers).get(replyType));
  }

  @Override
  public boolean isSuccessfulReply(boolean compensating, Message message) {
    return getParticipantInvocation(compensating).get().isSuccessfulReply(message);
  }

  @Override
  public Publisher<StepOutcome> makeStepOutcome(Data data, boolean compensating) {
    Publisher<CommandWithDestinationAndType> commandWithDestination =
            getParticipantInvocation(compensating).map(pi -> pi.makeCommandToSend(data)).orElse(Mono.empty());
    return Mono.from(commandWithDestination).map(cmd -> StepOutcome.makeRemoteStepOutcome(Collections.singletonList(cmd)));
  }
}
