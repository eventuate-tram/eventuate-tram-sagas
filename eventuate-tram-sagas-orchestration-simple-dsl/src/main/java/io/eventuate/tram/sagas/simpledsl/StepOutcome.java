package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class StepOutcome {

  public abstract void visit(Consumer<Optional<RuntimeException>> localConsumer, Consumer<List<CommandWithDestinationAndType>> commandsConsumer);

  static class LocalStepOutcome extends StepOutcome {
    private Optional<RuntimeException> localOutcome;

    public LocalStepOutcome(Optional<RuntimeException> localOutcome) {
      this.localOutcome = localOutcome;
    }

    @Override
    public void visit(Consumer<Optional<RuntimeException>> localConsumer, Consumer<List<CommandWithDestinationAndType>> commandsConsumer) {
        localConsumer.accept(localOutcome);
    }
  }

  static class RemoteStepOutcome extends StepOutcome {
    private List<CommandWithDestinationAndType> commandsToSend;

    public RemoteStepOutcome(List<CommandWithDestinationAndType> commandsToSend) {
      this.commandsToSend = commandsToSend;
    }

    @Override
    public void visit(Consumer<Optional<RuntimeException>> localConsumer, Consumer<List<CommandWithDestinationAndType>> commandsConsumer) {
      commandsConsumer.accept(commandsToSend);
    }
  }

  public static StepOutcome makeLocalOutcome(Optional<RuntimeException> localOutcome) {
    return new LocalStepOutcome(localOutcome);
  }
  public static StepOutcome makeRemoteStepOutcome(List<CommandWithDestinationAndType> commandsToSend) {
    return new RemoteStepOutcome(commandsToSend);
  }
}
