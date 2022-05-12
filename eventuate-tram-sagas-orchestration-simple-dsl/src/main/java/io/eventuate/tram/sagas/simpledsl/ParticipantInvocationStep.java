package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;

import java.util.*;
import java.util.function.BiConsumer;

public class ParticipantInvocationStep<SAGA_DATA> implements SagaStep<SAGA_DATA> {
  private final Map<String, BiConsumer<SAGA_DATA, Object>> actionReplyHandlers;
  private final Map<String, BiConsumer<SAGA_DATA, Object>> compensationReplyHandlers;
  private Optional<ParticipantInvocation<SAGA_DATA>> participantInvocation;
  private Optional<ParticipantInvocation<SAGA_DATA>> compensation;

  public ParticipantInvocationStep(Optional<ParticipantInvocation<SAGA_DATA>> participantInvocation,
                                   Optional<ParticipantInvocation<SAGA_DATA>> compensation,
                                   Map<String, BiConsumer<SAGA_DATA, Object>> actionReplyHandlers,
                                   Map<String, BiConsumer<SAGA_DATA, Object>> compensationReplyHandlers) {
    this.actionReplyHandlers = actionReplyHandlers;
    this.compensationReplyHandlers = compensationReplyHandlers;
    this.participantInvocation = participantInvocation;
    this.compensation = compensation;
  }

  private Optional<ParticipantInvocation<SAGA_DATA>> getParticipantInvocation(boolean compensating) {
    return compensating ? compensation : participantInvocation;
  }

  public boolean hasAction(SAGA_DATA data) {
    return participantInvocation.isPresent() && participantInvocation.map(p -> p.isInvocable(data)).orElse(true);
  }

  public boolean hasCompensation(SAGA_DATA data) {
    return compensation.isPresent() && compensation.map(p -> p.isInvocable(data)).orElse(true);
  }

  public Optional<BiConsumer<SAGA_DATA, Object>> getReplyHandler(Message message, boolean compensating) {
    String replyType = message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE);
    return Optional.ofNullable((compensating ? compensationReplyHandlers : actionReplyHandlers).get(replyType));
  }

  @Override
  public boolean isSuccessfulReply(boolean compensating, Message message) {
    return getParticipantInvocation(compensating).get().isSuccessfulReply(message);
  }

  @Override
  public StepOutcome makeStepOutcome(SAGA_DATA sagaData, boolean compensating) {
    return StepOutcome.makeRemoteStepOutcome(getParticipantInvocation(compensating)
            .map(x -> x.makeCommandToSend(sagaData))
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList));
  }


}
