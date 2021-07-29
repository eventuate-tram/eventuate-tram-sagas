package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class ReactiveStepOutcome {

  public abstract Publisher<?> visit(Function<Optional<RuntimeException>, Publisher<?>> localConsumer,
                                     Function<List<CommandWithDestination>, Publisher<?>> commandsConsumer);

  static class ReactiveLocalStepOutcome extends ReactiveStepOutcome {
    private Optional<RuntimeException> localOutcome;

    public ReactiveLocalStepOutcome(Optional<RuntimeException> localOutcome) {
      this.localOutcome = localOutcome;
    }

    @Override
    public Publisher<?> visit(Function<Optional<RuntimeException>, Publisher<?>> localConsumer,
                      Function<List<CommandWithDestination>, Publisher<?>> commandsConsumer) {
        return localConsumer.apply(localOutcome);
    }
  }

  static class ReactiveRemoteStepOutcome extends ReactiveStepOutcome {
    private List<CommandWithDestination> commandsToSend;

    public ReactiveRemoteStepOutcome(List<CommandWithDestination> commandsToSend) {
      this.commandsToSend = commandsToSend;
    }

    @Override
    public Publisher<?> visit(Function<Optional<RuntimeException>, Publisher<?>> localConsumer,
                      Function<List<CommandWithDestination>, Publisher<?>> commandsConsumer) {
      return commandsConsumer.apply(commandsToSend);
    }
  }

  public static ReactiveStepOutcome makeLocalOutcome(Optional<RuntimeException> localOutcome) {
    return new ReactiveLocalStepOutcome(localOutcome);
  }
  public static ReactiveStepOutcome makeRemoteStepOutcome(List<CommandWithDestination> commandsToSend) {
    return new ReactiveRemoteStepOutcome(commandsToSend);
  }
}
