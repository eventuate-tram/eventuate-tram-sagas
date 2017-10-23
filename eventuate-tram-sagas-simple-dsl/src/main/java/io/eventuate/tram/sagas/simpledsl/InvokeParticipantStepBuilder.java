package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class InvokeParticipantStepBuilder<Data> {
  private final SimpleSagaDefinitionBuilder<Data> parent;
  private Optional<ParticipantInvocation<Data>> action = Optional.empty();
  private Optional<ParticipantInvocation<Data>> compensation = Optional.empty();
  private Map<String, BiConsumer<Data, Object>> actionReplyHandlers = new HashMap<>();
  private Map<String, BiConsumer<Data, Object>> compensationReplyHandlers = new HashMap<>();

  public InvokeParticipantStepBuilder(SimpleSagaDefinitionBuilder<Data> parent) {
    this.parent = parent;
  }


  public InvokeParticipantStepBuilder<Data> withAction(Function<Data, CommandWithDestination> action) {
    this.action = Optional.of(new ParticipantInvocationImpl<>(action));
    return this;
  }

  public InvokeParticipantStepBuilder<Data> withCompensation(Function<Data, CommandWithDestination> compensation) {
    this.compensation = Optional.of(new ParticipantInvocationImpl<>(compensation));
    return this;
  }

  public <T> InvokeParticipantStepBuilder<Data> onReply(Class<T> replyClass, BiConsumer<Data, T> replyHandler) {
    if (compensation.isPresent())
      compensationReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.accept(data, (T) rawReply));
    else
      actionReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.accept(data, (T) rawReply));
    return this;
  }

  public StepBuilder<Data> step() {
    parent.addStep(new ParticipantInvocationStep<>(action, compensation, actionReplyHandlers, compensationReplyHandlers));
    return new StepBuilder<>(parent);
  }

  public SagaDefinition<Data> build() {
    // TODO see comment in local step
    parent.addStep(new ParticipantInvocationStep<>(action, compensation, actionReplyHandlers, compensationReplyHandlers));
    return parent.build();
  }

}
