package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.commands.common.Command;

public class PendingSagaCommand {
  private String destination;
  private String resource;
  private Command command;

  public PendingSagaCommand(String destination, String resource, Command command) {
    this.destination = destination;
    this.resource = resource;
    this.command = command;
  }

  public String getDestination() {
    return destination;
  }

  public String getResource() {
    return resource;
  }

  public Command getCommand() {
    return command;
  }
}
