package io.eventuate.tram.sagas.simpledsl;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;
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
    Assert.notNull(compensation);
    this.participantInvocation = participantInvocation;
    this.compensation = compensation;
  }

  public ParticipantInvocation<Data> getParticipantInvocation(boolean compensating) {
    return compensating ? compensation.get() : participantInvocation.get();
  }

  public Optional<ParticipantInvocation<Data>> getCompensatingParticipantInvocation() {
    return compensation;
  }

  public Optional<ParticipantInvocation<Data>> getAction() {
    return participantInvocation;
  }

  public boolean hasCompensation() {
    return compensation.isPresent();
  }

  public boolean hasAction() {
    return participantInvocation.isPresent();
  }

  public Optional<BiConsumer<Data, Object>> getReplyHandler(String replyType, boolean compensating) {
    return Optional.ofNullable((compensating ? compensationReplyHandlers : actionReplyHandlers).get(replyType));
  }
}
