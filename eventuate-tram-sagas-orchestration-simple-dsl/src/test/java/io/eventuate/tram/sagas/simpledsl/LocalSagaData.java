package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

public class LocalSagaData {

  public CommandWithDestination do2() {
    return new CommandWithDestination("participant2", null, new ReserveCreditCommand());
  }

  public CommandWithDestination undo2() {
    return new CommandWithDestination("participant2", null, new ReleaseCreditCommand());
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
