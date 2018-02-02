package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.messaging.common.Message;

import java.util.function.Function;

public class ParticipantEndpointInvocationImpl<Data, C extends Command> implements ParticipantInvocation<Data> {


  private final CommandEndpoint<C> commandEndpoint;
  private final Function<Data, C> commandProvider;

  public ParticipantEndpointInvocationImpl(CommandEndpoint<C> commandEndpoint, Function<Data, C> commandProvider) {
    this.commandEndpoint = commandEndpoint;
    this.commandProvider = commandProvider;
  }

  @Override
  public boolean isSuccessfulReply(Message message) {
    return CommandReplyOutcome.SUCCESS.name().equals(message.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME));
  }

  @Override
  public CommandWithDestination makeCommandToSend(Data data) {
    return new CommandWithDestination(commandEndpoint.getCommandChannel(), null, commandProvider.apply(data));
  }
}
