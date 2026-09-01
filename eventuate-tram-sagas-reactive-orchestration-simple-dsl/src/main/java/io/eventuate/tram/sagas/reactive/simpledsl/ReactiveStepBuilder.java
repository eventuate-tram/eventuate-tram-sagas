package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ReactiveStepBuilder<Data> implements ReactiveWithCompensationBuilder<Data> {

  private final SimpleReactiveSagaDefinitionBuilder<Data> parent;

  public ReactiveStepBuilder(SimpleReactiveSagaDefinitionBuilder<Data> builder) {
    this.parent = builder;
  }

  public ReactiveLocalStepBuilder<Data> invokeLocal(Function<Data, Publisher<?>> localFunction) {
    return new ReactiveLocalStepBuilder<>(parent, localFunction);
  }


  public InvokeReactiveParticipantStepBuilder<Data> invokeParticipant(Function<Data, Publisher<CommandWithDestination>> action) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withAction(Optional.empty(), action);
  }
  public InvokeReactiveParticipantStepBuilder<Data> notifyParticipant(Function<Data, Publisher<CommandWithDestination>> action) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withNotificationAction(Optional.empty(), action);
  }

  public InvokeReactiveParticipantStepBuilder<Data> invokeParticipant(Predicate<Data> participantInvocationPredicate,
                                                                      Function<Data, Publisher<CommandWithDestination>> action) {
    return new InvokeReactiveParticipantStepBuilder<>(parent)
            .withAction(Optional.of(participantInvocationPredicate), action);
  }

  public <C extends Command> InvokeReactiveParticipantStepBuilder<Data> invokeParticipant(CommandEndpoint<C> commandEndpoint,
                                                                                          Function<Data, Publisher<C>> commandProvider) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withAction(Optional.empty(), commandEndpoint, commandProvider);
  }

  public <C extends Command> InvokeReactiveParticipantStepBuilder<Data> invokeParticipant(Predicate<Data> participantInvocationPredicate,
                                                                                          CommandEndpoint<C> commandEndpoint, Function<Data, Publisher<C>> commandProvider) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withAction(Optional.of(participantInvocationPredicate), commandEndpoint, commandProvider);
  }

  @Override
  public InvokeReactiveParticipantStepBuilder<Data> withCompensation(Function<Data, Publisher<CommandWithDestination>> compensation) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withCompensation(compensation);
  }

  @Override
  public InvokeReactiveParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate,
                                                                     Function<Data, Publisher<CommandWithDestination>> compensation) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withCompensation(compensationPredicate, compensation);
  }


  @Override
  public <C extends Command> InvokeReactiveParticipantStepBuilder<Data> withCompensation(CommandEndpoint<C> commandEndpoint,
                                                                                         Function<Data, Publisher<C>> commandProvider) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withCompensation(commandEndpoint, commandProvider);
  }

  @Override
  public <C extends Command> InvokeReactiveParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate,
                                                                                         CommandEndpoint<C> commandEndpoint, Function<Data, Publisher<C>> commandProvider) {
    return new InvokeReactiveParticipantStepBuilder<>(parent).withCompensation(compensationPredicate, commandEndpoint, commandProvider);
  }

}
