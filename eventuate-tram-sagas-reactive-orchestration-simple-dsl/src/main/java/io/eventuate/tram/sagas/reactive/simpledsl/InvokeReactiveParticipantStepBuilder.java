package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class InvokeReactiveParticipantStepBuilder<SAGA_DATA> implements ReactiveWithCompensationBuilder<SAGA_DATA> {

  private final SimpleReactiveSagaDefinitionBuilder<SAGA_DATA> parent;
  private Optional<ReactiveParticipantInvocation<SAGA_DATA>> action = Optional.empty();
  private Optional<ReactiveParticipantInvocation<SAGA_DATA>> compensation = Optional.empty();
  private Map<String, BiFunction<SAGA_DATA, Object, Publisher<?>>> actionReplyHandlers = new HashMap<>();
  private Map<String, BiFunction<SAGA_DATA, Object, Publisher<?>>> compensationReplyHandlers = new HashMap<>();

  public InvokeReactiveParticipantStepBuilder(SimpleReactiveSagaDefinitionBuilder<SAGA_DATA> parent) {
    this.parent = parent;
  }

  InvokeReactiveParticipantStepBuilder<SAGA_DATA> withAction(Optional<Predicate<SAGA_DATA>> participantInvocationPredicate,
                                                             Function<SAGA_DATA, Publisher<CommandWithDestination>> action) {
    this.action = Optional.of(new ReactiveParticipantInvocationImpl<>(participantInvocationPredicate, action));
    return this;
  }

  <C extends Command> InvokeReactiveParticipantStepBuilder<SAGA_DATA> withAction(Optional<Predicate<SAGA_DATA>> participantInvocationPredicate,
                                                                                 CommandEndpoint<C> commandEndpoint,
                                                                                 Function<SAGA_DATA, Publisher<C>> commandProvider) {
    this.action = Optional.of(new ReactiveParticipantEndpointInvocationImpl<>(participantInvocationPredicate, commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public InvokeReactiveParticipantStepBuilder<SAGA_DATA> withCompensation(Function<SAGA_DATA, Publisher<CommandWithDestination>> compensation) {
    this.compensation = Optional.of(new ReactiveParticipantInvocationImpl<>(Optional.empty(), compensation));
    return this;
  }

  @Override
  public InvokeReactiveParticipantStepBuilder<SAGA_DATA> withCompensation(Predicate<SAGA_DATA> compensationPredicate, Function<SAGA_DATA, Publisher<CommandWithDestination>> compensation) {
    this.compensation = Optional.of(new ReactiveParticipantInvocationImpl<>(Optional.of(compensationPredicate), compensation));
    return this;
  }

  @Override
  public <C extends Command> InvokeReactiveParticipantStepBuilder<SAGA_DATA> withCompensation(CommandEndpoint<C> commandEndpoint, Function<SAGA_DATA, Publisher<C>> commandProvider) {
    this.compensation = Optional.of(new ReactiveParticipantEndpointInvocationImpl<>(Optional.empty(), commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public <C extends Command> InvokeReactiveParticipantStepBuilder<SAGA_DATA> withCompensation(Predicate<SAGA_DATA> compensationPredicate,
                                                                                              CommandEndpoint<C> commandEndpoint,
                                                                                              Function<SAGA_DATA, Publisher<C>> commandProvider) {
    this.compensation = Optional.of(new ReactiveParticipantEndpointInvocationImpl<>(Optional.of(compensationPredicate), commandEndpoint, commandProvider));
    return this;
  }

  public <T> InvokeReactiveParticipantStepBuilder<SAGA_DATA> onReply(Class<T> replyClass, BiFunction<SAGA_DATA, T, Publisher<?>> replyHandler) {
    if (compensation.isPresent())
      compensationReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.apply(data, (T) rawReply));
    else
      actionReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.apply(data, (T) rawReply));
    return this;
  }

  public ReactiveStepBuilder<SAGA_DATA> step() {
    addStep();
    return new ReactiveStepBuilder<>(parent);
  }

  public SagaDefinition<Publisher<SagaActions<SAGA_DATA>>, SAGA_DATA> build() {
    addStep();
    return parent.build();
  }

  private void addStep() {
    parent.addStep(new ReactiveParticipantInvocationStep<>(action, compensation, actionReplyHandlers, compensationReplyHandlers));
  }

}
