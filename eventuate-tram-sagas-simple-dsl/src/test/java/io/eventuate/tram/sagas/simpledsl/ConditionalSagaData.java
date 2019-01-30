package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

public class ConditionalSagaData {

  private boolean invoke1;

  public ConditionalSagaData(boolean invoke1) {
    this.invoke1 = invoke1;
  }

  public ConditionalSagaData() {
  }

  public boolean isInvoke1() {
    return invoke1;
  }

  public void setInvoke1(boolean invoke1) {
    this.invoke1 = invoke1;
  }

  public CommandWithDestination do1() {
    return new CommandWithDestination("participant1", null, new Do1Command());
  }

  public CommandWithDestination undo1() {
    return new CommandWithDestination("participant1", null, new Undo1Command());
  }

  public CommandWithDestination do2() {
    return new CommandWithDestination("participant2", null, new Do2Command());
  }
}
