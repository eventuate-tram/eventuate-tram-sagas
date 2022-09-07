package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class InvokeParticipantStepBuilder<Data> implements WithCompensationBuilder<Data> {

  private final SimpleSagaDefinitionBuilder<Data> parent;
  private Optional<ParticipantInvocation<Data>> action = Optional.empty();
  private Optional<ParticipantInvocation<Data>> compensation = Optional.empty();
  private Map<String, BiConsumer<Data, Object>> actionReplyHandlers = new HashMap<>();
  private Map<String, BiConsumer<Data, Object>> compensationReplyHandlers = new HashMap<>();

  public InvokeParticipantStepBuilder(SimpleSagaDefinitionBuilder<Data> parent) {
    this.parent = parent;
  }

  InvokeParticipantStepBuilder<Data> withAction(Optional<Predicate<Data>> participantInvocationPredicate, Function<Data, CommandWithDestination> action) {
    this.action = Optional.of(new ParticipantInvocationImpl<>(participantInvocationPredicate, action));
    return this;
  }

  public InvokeParticipantStepBuilder<Data> withNotificationAction(Optional<Predicate<Data>> participantInvocationPredicate, Function<Data, CommandWithDestination> notificationAction) {
    this.action = Optional.of(new ParticipantInvocationImpl<>(participantInvocationPredicate, notificationAction, true));
    return this;
  }


  <C extends Command> InvokeParticipantStepBuilder<Data> withAction(Optional<Predicate<Data>> participantInvocationPredicate, CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    this.action = Optional.of(new ParticipantEndpointInvocationImpl<>(participantInvocationPredicate, commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public InvokeParticipantStepBuilder<Data> withCompensation(Function<Data, CommandWithDestination> compensation) {
    this.compensation = Optional.of(new ParticipantInvocationImpl<>(Optional.empty(), compensation));
    return this;
  }

  public InvokeParticipantStepBuilder<Data> withCompensationNotification(Function<Data, CommandWithDestination> compensation) {
    this.compensation = Optional.of(new ParticipantInvocationImpl<>(Optional.empty(), compensation, true));
    return this;
  }

  public InvokeParticipantStepBuilder<Data> withCompensationNotification(Predicate<Data> compensationPredicate, Function<Data, CommandWithDestination> compensation) {
    this.compensation = Optional.of(new ParticipantInvocationImpl<>(Optional.of(compensationPredicate), compensation, true));
    return this;
  }

  @Override
  public InvokeParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate, Function<Data, CommandWithDestination> compensation) {
    this.compensation = Optional.of(new ParticipantInvocationImpl<>(Optional.of(compensationPredicate), compensation));
    return this;
  }

  @Override
  public <C extends Command> InvokeParticipantStepBuilder<Data> withCompensation(CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    this.compensation = Optional.of(new ParticipantEndpointInvocationImpl<>(Optional.empty(), commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public <C extends Command> InvokeParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate, CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    this.compensation = Optional.of(new ParticipantEndpointInvocationImpl<>(Optional.of(compensationPredicate), commandEndpoint, commandProvider));
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
    addStep();
    return new StepBuilder<>(parent);
  }

  public SagaDefinition<Data> build() {
    // TODO see comment in local step
    addStep();
    return parent.build();
  }

  private void addStep() {
    parent.addStep(new ParticipantInvocationStep<>(action, compensation, actionReplyHandlers, compensationReplyHandlers));
  }

}
