package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;

import java.util.*;
import java.util.function.BiConsumer;

public class ParticipantInvocationStep<Data> implements SagaStep<Data> {
  private final Map<String, BiConsumer<Data, Object>> actionReplyHandlers;
  private final Map<String, BiConsumer<Data, Object>> compensationReplyHandlers;
  private Optional<ParticipantInvocation<Data>> participantInvocation;
  private Optional<ParticipantInvocation<Data>> compensation;

  public ParticipantInvocationStep(Optional<ParticipantInvocation<Data>> participantInvocation,
                                   Optional<ParticipantInvocation<Data>> compensation,
                                   Map<String, BiConsumer<Data, Object>> actionReplyHandlers,
                                   Map<String, BiConsumer<Data, Object>> compensationReplyHandlers) {
    this.actionReplyHandlers = actionReplyHandlers;
    this.compensationReplyHandlers = compensationReplyHandlers;
    this.participantInvocation = participantInvocation;
    this.compensation = compensation;
  }

  private Optional<ParticipantInvocation<Data>> getParticipantInvocation(boolean compensating) {
    return compensating ? compensation : participantInvocation;
  }

  public boolean hasAction(Data data) {
    return participantInvocation.isPresent() && participantInvocation.map(p -> p.isInvocable(data)).orElse(true);
  }

  public boolean hasCompensation(Data data) {
    return compensation.isPresent() && compensation.map(p -> p.isInvocable(data)).orElse(true);
  }

  public Optional<BiConsumer<Data, Object>> getReplyHandler(Message message, boolean compensating) {
    String replyType = message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE);
    return Optional.ofNullable((compensating ? compensationReplyHandlers : actionReplyHandlers).get(replyType));
  }

  @Override
  public boolean isSuccessfulReply(boolean compensating, Message message) {
    return getParticipantInvocation(compensating).get().isSuccessfulReply(message);
  }

  @Override
  public StepOutcome makeStepOutcome(Data data, boolean compensating) {
    return StepOutcome.makeRemoteStepOutcome(getParticipantInvocation(compensating)
            .map(x -> x.makeCommandToSend(data))
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList));
  }


}
