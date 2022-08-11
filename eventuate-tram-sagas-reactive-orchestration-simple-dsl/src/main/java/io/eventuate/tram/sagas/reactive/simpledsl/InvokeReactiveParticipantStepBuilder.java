package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class InvokeReactiveParticipantStepBuilder<Data> implements ReactiveWithCompensationBuilder<Data> {

  private final SimpleReactiveSagaDefinitionBuilder<Data> parent;
  private Optional<ReactiveParticipantInvocation<Data>> action = Optional.empty();
  private Optional<ReactiveParticipantInvocation<Data>> compensation = Optional.empty();
  private Map<String, BiFunction<Data, Object, Publisher<?>>> actionReplyHandlers = new HashMap<>();
  private Map<String, BiFunction<Data, Object, Publisher<?>>> compensationReplyHandlers = new HashMap<>();

  public InvokeReactiveParticipantStepBuilder(SimpleReactiveSagaDefinitionBuilder<Data> parent) {
    this.parent = parent;
  }

  InvokeReactiveParticipantStepBuilder<Data> withAction(Optional<Predicate<Data>> participantInvocationPredicate,
                                                        Function<Data, Publisher<CommandWithDestination>> action) {
    this.action = Optional.of(new ReactiveParticipantInvocationImpl<>(participantInvocationPredicate, action));
    return this;
  }

  InvokeReactiveParticipantStepBuilder<Data> withNotificationAction(Optional<Predicate<Data>> participantInvocationPredicate,
                                                        Function<Data, Publisher<CommandWithDestination>> notificationAction) {
    this.action = Optional.of(new ReactiveParticipantInvocationImpl<>(participantInvocationPredicate, notificationAction, true));
    return this;
  }


  <C extends Command> InvokeReactiveParticipantStepBuilder<Data> withAction(Optional<Predicate<Data>> participantInvocationPredicate,
                                                                            CommandEndpoint<C> commandEndpoint,
                                                                            Function<Data, Publisher<C>> commandProvider) {
    this.action = Optional.of(new ReactiveParticipantEndpointInvocationImpl<>(participantInvocationPredicate, commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public InvokeReactiveParticipantStepBuilder<Data> withCompensation(Function<Data, Publisher<CommandWithDestination>> compensation) {
    this.compensation = Optional.of(new ReactiveParticipantInvocationImpl<>(Optional.empty(), compensation));
    return this;
  }

  public InvokeReactiveParticipantStepBuilder<Data> withCompensationNotification(Function<Data, Publisher<CommandWithDestination>> compensation) {
    this.compensation = Optional.of(new ReactiveParticipantInvocationImpl<>(Optional.empty(), compensation, true));
    return this;
  }

  @Override
  public InvokeReactiveParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate, Function<Data, Publisher<CommandWithDestination>> compensation) {
    this.compensation = Optional.of(new ReactiveParticipantInvocationImpl<>(Optional.of(compensationPredicate), compensation));
    return this;
  }

  @Override
  public <C extends Command> InvokeReactiveParticipantStepBuilder<Data> withCompensation(CommandEndpoint<C> commandEndpoint, Function<Data, Publisher<C>> commandProvider) {
    this.compensation = Optional.of(new ReactiveParticipantEndpointInvocationImpl<>(Optional.empty(), commandEndpoint, commandProvider));
    return this;
  }

  @Override
  public <C extends Command> InvokeReactiveParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate,
                                                                                         CommandEndpoint<C> commandEndpoint,
                                                                                         Function<Data, Publisher<C>> commandProvider) {
    this.compensation = Optional.of(new ReactiveParticipantEndpointInvocationImpl<>(Optional.of(compensationPredicate), commandEndpoint, commandProvider));
    return this;
  }

  public <T> InvokeReactiveParticipantStepBuilder<Data> onReply(Class<T> replyClass, BiFunction<Data, T, Publisher<?>> replyHandler) {
    if (compensation.isPresent())
      compensationReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.apply(data, (T) rawReply));
    else
      actionReplyHandlers.put(replyClass.getName(), (data, rawReply) -> replyHandler.apply(data, (T) rawReply));
    return this;
  }

  public ReactiveStepBuilder<Data> step() {
    addStep();
    return new ReactiveStepBuilder<>(parent);
  }

  public ReactiveSagaDefinition<Data> build() {
    addStep();
    return parent.build();
  }

  private void addStep() {
    parent.addStep(new ReactiveParticipantInvocationStep<>(action, compensation, actionReplyHandlers, compensationReplyHandlers));
  }


}
