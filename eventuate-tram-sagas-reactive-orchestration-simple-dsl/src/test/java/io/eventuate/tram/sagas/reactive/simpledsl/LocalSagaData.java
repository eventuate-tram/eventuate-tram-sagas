package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import reactor.core.publisher.Mono;

public class LocalSagaData {

  public Mono<CommandWithDestination> do2() {
    return Mono.just(new CommandWithDestination("participant2", null, new ReserveCreditCommand()));
  }

  public Mono<CommandWithDestination> undo2() {
    return Mono.just(new CommandWithDestination("participant2", null, new ReleaseCreditCommand()));
  }

  public Mono<CommandWithDestination> notify3() {
    return Mono.just(new CommandWithDestination("participant3", null, new NotifyCommand()));
  }

}
