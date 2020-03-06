package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

public class LocalSagaData {

  public CommandWithDestination do2() {
    return new CommandWithDestination("participant2", null, new Do2Command());
  }

  public CommandWithDestination undo2() {
    return new CommandWithDestination("participant2", null, new Undo2Command());
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public boolean equals(Object obj) {
    return true;
  }
}
