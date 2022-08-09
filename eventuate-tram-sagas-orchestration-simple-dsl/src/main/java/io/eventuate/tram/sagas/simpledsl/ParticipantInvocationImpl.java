package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParticipantInvocationImpl<Data, C extends Command> extends AbstractParticipantInvocation<Data> {
  private final boolean notification;
  private final Function<Data, CommandWithDestination> commandBuilder;


  public ParticipantInvocationImpl(Optional<Predicate<Data>> invocablePredicate, Function<Data, CommandWithDestination> commandBuilder) {
    this(invocablePredicate, commandBuilder, false);
  }

  public ParticipantInvocationImpl(Optional<Predicate<Data>> invocablePredicate, Function<Data, CommandWithDestination> commandBuilder, boolean notification) {
    super(invocablePredicate);
    this.commandBuilder = commandBuilder;
    this.notification = notification;
  }

    @Override
  public boolean isSuccessfulReply(Message message) {
    return CommandReplyOutcome.SUCCESS.name().equals(message.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME));
  }

  @Override
  public CommandWithDestinationAndType makeCommandToSend(Data data) {
    return new CommandWithDestinationAndType(commandBuilder.apply(data), notification);
  }
}
