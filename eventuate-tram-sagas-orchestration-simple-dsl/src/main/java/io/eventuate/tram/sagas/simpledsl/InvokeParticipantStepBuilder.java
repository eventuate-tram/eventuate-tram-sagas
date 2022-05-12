package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class InvokeParticipantStepBuilder<SAGA_DATA> implements WithCompensationBuilder<SAGA_DATA> {

  private final SimpleSagaDefinitionBuilder<SAGA_DATA> parent;
  private Optional<ParticipantInvocation<SAGA_DATA>> action = Optional.empty();
  private Optional<ParticipantInvocation<SAGA_DATA>> compensation = Optional.empty();
  private Map<String, BiConsumer<SAGA_DATA, Object>> actionReplyHandlers = new HashMap<>();
  private Map<String, BiConsumer<SAGA_DATA, Object>> compensationReplyHandlers = new HashMap<>();

  public InvokeParticipantStepBuilder(SimpleSagaDefinitionBuilder<SAGA_DATA> parent) {
    this.parent = parent;
  }

  InvokeParticipantStepBuilder<SAGA_DATA> withAction(Optional<Predicate<SAGA_DATA>> participantInvocationPredicate, Function<SAGA_DATA, CommandWithDestination> action) {
    this.action = Optional.of(new ParticipantInvocationImpl<>(participantInvocationPredicate, action));
    return this;
  }

  <C extends Command> InvokeParticipantStepBuilder<SAGA_DATA> withAction(Optional<Predicate<SAGA_DATA>> participantInvocationPredicate, CommandEndpoint<C> commandEndpoint, Function<SAGA_DATA, C> commandProvider) {
    this.action = Optional.of(new ParticipantEndpointInvocationImpl<>(participantInvocationPredicate, commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public InvokeParticipantStepBuilder<SAGA_DATA> withCompensation(Function<SAGA_DATA, CommandWithDestination> compensation) {
    this.compensation = Optional.of(new ParticipantInvocationImpl<>(Optional.empty(), compensation));
    return this;
  }

  @Override
  public InvokeParticipantStepBuilder<SAGA_DATA> withCompensation(Predicate<SAGA_DATA> compensationPredicate, Function<SAGA_DATA, CommandWithDestination> compensation) {
    this.compensation = Optional.of(new ParticipantInvocationImpl<>(Optional.of(compensationPredicate), compensation));
    return this;
  }

  @Override
  public <C extends Command> InvokeParticipantStepBuilder<SAGA_DATA> withCompensation(CommandEndpoint<C> commandEndpoint, Function<SAGA_DATA, C> commandProvider) {
    this.compensation = Optional.of(new ParticipantEndpointInvocationImpl<>(Optional.empty(), commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public <C extends Command> InvokeParticipantStepBuilder<SAGA_DATA> withCompensation(Predicate<SAGA_DATA> compensationPredicate, CommandEndpoint<C> commandEndpoint, Function<SAGA_DATA, C> commandProvider) {
    this.compensation = Optional.of(new ParticipantEndpointInvocationImpl<>(Optional.of(compensationPredicate), commandEndpoint, commandProvider));
    return this;
  }

  public <T> InvokeParticipantStepBuilder<SAGA_DATA> onReply(Class<T> replyClass, BiConsumer<SAGA_DATA, T> replyHandler) {
    if (compensation.isPresent())
      compensationReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.accept(data, (T) rawReply));
    else
      actionReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.accept(data, (T) rawReply));
    return this;
  }

  public StepBuilder<SAGA_DATA> step() {
    addStep();
    return new StepBuilder<>(parent);
  }

  public SagaDefinition<SagaActions<SAGA_DATA>, SAGA_DATA> build() {
    // TODO see comment in local step
    addStep();
    return parent.build();
  }

  private void addStep() {
    parent.addStep(new ParticipantInvocationStep<>(action, compensation, actionReplyHandlers, compensationReplyHandlers));
  }

}
