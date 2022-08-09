package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StepBuilder<Data> implements WithCompensationBuilder<Data> {

  private final SimpleSagaDefinitionBuilder<Data> parent;

  public StepBuilder(SimpleSagaDefinitionBuilder<Data> builder) {
    this.parent = builder;
  }

  public LocalStepBuilder<Data> invokeLocal(Consumer<Data> localFunction) {
    return new LocalStepBuilder<>(parent, localFunction);
  }


  public InvokeParticipantStepBuilder<Data> invokeParticipant(Function<Data, CommandWithDestination> action) {
    return new InvokeParticipantStepBuilder<>(parent).withAction(Optional.empty(), action);
  }

  public InvokeParticipantStepBuilder<Data> invokeParticipant(Predicate<Data> participantInvocationPredicate, Function<Data, CommandWithDestination> action) {
    return new InvokeParticipantStepBuilder<>(parent).withAction(Optional.of(participantInvocationPredicate), action);
  }

  public <C extends Command> InvokeParticipantStepBuilder<Data> invokeParticipant(CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    return new InvokeParticipantStepBuilder<>(parent).withAction(Optional.empty(), commandEndpoint, commandProvider);
  }

  public <C extends Command> InvokeParticipantStepBuilder<Data> invokeParticipant(Predicate<Data> participantInvocationPredicate, CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    return new InvokeParticipantStepBuilder<>(parent).withAction(Optional.of(participantInvocationPredicate), commandEndpoint, commandProvider);
  }

  @Override
  public InvokeParticipantStepBuilder<Data> withCompensation(Function<Data, CommandWithDestination> compensation) {
    return new InvokeParticipantStepBuilder<>(parent).withCompensation(compensation);
  }

  @Override
  public InvokeParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate, Function<Data, CommandWithDestination> compensation) {
    return new InvokeParticipantStepBuilder<>(parent).withCompensation(compensation);
  }


  @Override
  public <C extends Command> InvokeParticipantStepBuilder<Data> withCompensation(CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    return new InvokeParticipantStepBuilder<>(parent).withCompensation(commandEndpoint, commandProvider);
  }

  @Override
  public <C extends Command> InvokeParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate, CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    return new InvokeParticipantStepBuilder<>(parent).withCompensation(compensationPredicate, commandEndpoint, commandProvider);
  }

  public InvokeParticipantStepBuilder<Data> notifyParticipant(Function<Data, CommandWithDestination> notificationAction) {
    return new InvokeParticipantStepBuilder<>(parent).withNotificationAction(Optional.empty(), notificationAction);
  }
}
