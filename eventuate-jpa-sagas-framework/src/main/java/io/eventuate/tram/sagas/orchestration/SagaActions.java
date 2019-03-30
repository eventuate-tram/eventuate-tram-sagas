package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SagaActions<Data> {


  private final List<CommandWithDestination> commands;
  private final Optional<Data> updatedSagaData;
  private final Optional<String> updatedState;
  private boolean endState;
  private boolean compensating;
  private boolean local;
  private Optional<RuntimeException> localException;

  public SagaActions(List<CommandWithDestination> commands,
                     Optional<Data> updatedSagaData,
                     Optional<String> updatedState, boolean endState, boolean compensating, boolean local, Optional<RuntimeException> localException) {
    this.commands = commands;
    this.updatedSagaData = updatedSagaData;
    this.updatedState = updatedState;
    this.endState = endState;
    this.compensating = compensating;
    this.local = local;
    this.localException = localException;
  }

  List<CommandWithDestination> getCommands() {
    return commands;
  }

  Optional<Data> getUpdatedSagaData() {
    return updatedSagaData;
  }

  Optional<String> getUpdatedState() {
    return updatedState;
  }

  public boolean isEndState() {
    return endState;
  }

  public boolean isCompensating() {
    return compensating;
  }

  public boolean isLocal() {
    return local;
  }

  public Optional<RuntimeException> getLocalException() {
    return localException;
  }

  public static class Builder<Data> {
    private List<CommandWithDestination> commands = new ArrayList<>();
    private Optional<Data> updatedSagaData = Optional.empty();
    private Optional<String> updatedState = Optional.empty();
    private boolean endState;
    private boolean compensating;
    private boolean local;
    private Optional<RuntimeException> localException = Optional.empty();

    public Builder() {
    }

    public SagaActions<Data> build() {
      return new SagaActions<>(commands, updatedSagaData, updatedState, endState, compensating, local, localException);
    }

    public Builder<Data> withCommand(CommandWithDestination command) {
      commands.add(command);
      return this;
    }

    public Builder<Data> withUpdatedSagaData(Data data) {
      this.updatedSagaData = Optional.of(data);
      return this;
    }

    public Builder<Data> withUpdatedState(String state) {
      this.updatedState= Optional.of(state);
      return this;
    }

    public Builder<Data> withCommands(List<CommandWithDestination> commands) {
      this.commands.addAll(commands);
      return this;
    }

    public Builder<Data> withIsEndState(boolean endState) {
      this.endState = endState;
      return this;
    }

    public Builder<Data> withIsCompensating(boolean compensating) {
      this.compensating = compensating;
      return this;
    }


    public Builder<Data> withIsLocal(Optional<RuntimeException> localException) {
      this.localException = localException;
      this.local = true;
      return this;
    }
  }


  public static <Data> Builder<Data> builder() {
    return new Builder<>();
  }



}
