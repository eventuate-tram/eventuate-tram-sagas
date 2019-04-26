package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class StepOutcome {

  public abstract void visit(Consumer<Optional<RuntimeException>> localConsumer, Consumer<List<CommandWithDestination>> commandsConsumer);

  static class LocalStepOutcome extends StepOutcome {
    private Optional<RuntimeException> localOutcome;

    public LocalStepOutcome(Optional<RuntimeException> localOutcome) {
      this.localOutcome = localOutcome;
    }

    @Override
    public void visit(Consumer<Optional<RuntimeException>> localConsumer, Consumer<List<CommandWithDestination>> commandsConsumer) {
        localConsumer.accept(localOutcome);
    }
  }

  static class RemoteStepOutcome extends StepOutcome {
    private List<CommandWithDestination> commandsToSend;

    public RemoteStepOutcome(List<CommandWithDestination> commandsToSend) {
      this.commandsToSend = commandsToSend;
    }

    @Override
    public void visit(Consumer<Optional<RuntimeException>> localConsumer, Consumer<List<CommandWithDestination>> commandsConsumer) {
      commandsConsumer.accept(commandsToSend);
    }
  }

  public static StepOutcome makeLocalOutcome(Optional<RuntimeException> localOutcome) {
    return new LocalStepOutcome(localOutcome);
  }
  public static StepOutcome makeRemoteStepOutcome(List<CommandWithDestination> commandsToSend) {
    return new RemoteStepOutcome(commandsToSend);
  }
}
