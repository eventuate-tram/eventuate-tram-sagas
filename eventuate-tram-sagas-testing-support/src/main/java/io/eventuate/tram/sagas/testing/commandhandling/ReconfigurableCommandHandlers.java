package io.eventuate.tram.sagas.testing.commandhandling;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandExceptionHandler;
import io.eventuate.tram.commands.consumer.CommandHandler;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.messaging.common.Message;

import java.util.*;

public class ReconfigurableCommandHandlers extends CommandHandlers {

  private final Set<String> commandChannels;
  private List<SagaParticipantStubCommandHandler> handlers = new ArrayList<>();

  public ReconfigurableCommandHandlers(Set<String> commandChannels) {
    super(Collections.emptyList());
    this.commandChannels = commandChannels;
  }

  @Override
  public Set<String> getChannels() {
    return commandChannels;
  }

  public void add(SagaParticipantStubCommandHandler commandHandler) {
    this.handlers.add(commandHandler);
  }

  @Override
  public Optional<CommandHandler> findTargetMethod(Message message) {
    return handlers.stream().filter(h -> h.handles(message)).findFirst().map(x -> (CommandHandler)x);
  }

  @Override
  public Optional<CommandExceptionHandler> findExceptionHandler(Throwable cause) {
    return super.findExceptionHandler(cause);
  }

  public void reset() {
    handlers.clear();
  }

  public <C extends Command> Optional<SagaParticipantStubCommandHandler> findCommandHandler(String channel, Class<C> commandClass) {
    return handlers.stream().filter(h -> h.handles(channel, commandClass)).findFirst();
  }
}
