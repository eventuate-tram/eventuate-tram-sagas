package io.eventuate.tram.sagas.testing.commandhandling;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandHandler;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static org.junit.Assert.fail;

public class SagaParticipantStubCommandHandler<C> extends CommandHandler {

  private String commandChannel;
  private final Predicate<Message> expectedCommand;
  private Function<CommandMessage<C>, Message> replyBuilder;
  private final List<CommandMessage<C>> receivedCommands = new ArrayList<>();

  public SagaParticipantStubCommandHandler(String commandChannel, Class<C> expectedCommandClass, Predicate<Message> expectedCommand, Function<CommandMessage<C>, Message> replyBuilder) {
    super(commandChannel, Optional.empty(), expectedCommandClass, (cm, pv) -> {
      // Dummy - override invoke method
      return null;
    });
    this.commandChannel = commandChannel;
    this.expectedCommand = expectedCommand;
    this.replyBuilder = replyBuilder;
  }

  @Override
  public List<Message> invokeMethod(CommandMessage cm, Map<String, String> pathVars) {
    receivedCommands.add(cm);
    return singletonList(replyBuilder.apply(cm));
  }

  @Override
  public boolean handles(Message message) {
    return message.getRequiredHeader(Message.DESTINATION).equals(commandChannel) && super.handles(message) && expectedCommand.test(message);
  }

  public <C extends Command> boolean handles(String channel, Class<C> commandClass) {
    return commandChannel.equals(channel) && this.getCommandClass().equals(commandClass);
  }

  public void verifyCommandReceived() {
    if (receivedCommands.isEmpty())
      throw new RuntimeException("Did not receive command");
  }
}
