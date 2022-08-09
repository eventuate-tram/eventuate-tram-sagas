package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder;

import java.util.Collections;
import java.util.Map;

public class ConditionalSagaData {

  private boolean invoke1;

  public ConditionalSagaData(boolean invoke1) {
    this.invoke1 = invoke1;
  }

  public ConditionalSagaData() {
  }

  public static Map<String, String> DO1_COMMAND_EXTRA_HEADERS = Collections.singletonMap("k", "v");

  public boolean isInvoke1() {
    return invoke1;
  }

  public void setInvoke1(boolean invoke1) {
    this.invoke1 = invoke1;
  }

  public CommandWithDestination do1() {
    return CommandWithDestinationBuilder.send(new Do1Command()).to("participant1").withExtraHeaders(DO1_COMMAND_EXTRA_HEADERS).build();
  }

  public CommandWithDestination undo1() {
    return new CommandWithDestination("participant1", null, new Undo1Command());
  }

  public CommandWithDestination do2() {
    return new CommandWithDestination("participant2", null, new ReserveCreditCommand());
  }
}
