package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Command;

import java.util.Set;

public class CommandEndpoint<C extends Command> {

  private String commandChannel;
  private Class<C> commandClass;
  private Set<Class> replyClasses;

  public CommandEndpoint(String commandChannel, Class<C> commandClass, Set<Class> replyClasses) {
    this.commandChannel = commandChannel;
    this.commandClass = commandClass;
    this.replyClasses = replyClasses;
  }

  public String getCommandChannel() {
    return commandChannel;
  }
}
