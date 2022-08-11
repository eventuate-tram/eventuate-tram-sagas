package io.eventuate.tram.sagas.orchestration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SagaActions<Data> {


  private final List<CommandWithDestinationAndType> commands;
  private final Optional<Data> updatedSagaData;
  private final Optional<String> updatedState;
  private final boolean endState;
  private final boolean compensating;
  private final boolean local;
  private Optional<RuntimeException> localException;
  private final boolean failed;

  public SagaActions(List<CommandWithDestinationAndType> commands,
                     Optional<Data> updatedSagaData,
                     Optional<String> updatedState, boolean endState, boolean compensating, boolean failed, boolean local, Optional<RuntimeException> localException) {
    this.commands = commands;
    this.updatedSagaData = updatedSagaData;
    this.updatedState = updatedState;
    this.endState = endState;
    this.compensating = compensating;
    this.local = local;
    this.localException = localException;
    this.failed = failed;
  }

  public List<CommandWithDestinationAndType> getCommands() {
    return commands;
  }

  public Optional<Data> getUpdatedSagaData() {
    return updatedSagaData;
  }

  public Optional<String> getUpdatedState() {
    return updatedState;
  }

  public boolean isEndState() {
    return endState;
  }

  public boolean isCompensating() {
    return compensating;
  }

  public boolean isReplyExpected() {
    return (commands.isEmpty() || commands.stream().anyMatch(CommandWithDestinationAndType::isCommand)) && !local;
  }

  public boolean isFailed() {
    return failed;
  }

  public Optional<RuntimeException> getLocalException() {
    return localException;
  }

  public static class Builder<Data> {
    private List<CommandWithDestinationAndType> commands = new ArrayList<CommandWithDestinationAndType>();
    private Optional<Data> updatedSagaData = Optional.empty();
    private Optional<String> updatedState = Optional.empty();
    private boolean endState;
    private boolean compensating;
    private boolean local;
    private boolean failed;
    private Optional<RuntimeException> localException = Optional.empty();

    public Builder() {
    }

    public SagaActions<Data> build() {
      return new SagaActions<>(commands, updatedSagaData, updatedState, endState, compensating, failed, local, localException);
    }

    public Builder<Data> withCommand(CommandWithDestinationAndType command) {
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

    public Builder<Data> withCommands(List<CommandWithDestinationAndType> commands) {
      this.commands.addAll(commands);
      return this;
    }

    public Builder<Data> withIsEndState(boolean endState) {
      this.endState = endState;
      return this;
    }

    public Builder<Data> withIsFailed(boolean failed) {
      this.failed = failed;
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

    public SagaActions<Data> buildActions(Data data, boolean compensating, String state, boolean endState) {
      return withUpdatedSagaData(data)
              .withUpdatedState(state)
              .withIsEndState(endState)
              .withIsCompensating(compensating)
              .build();
    }

  }


  public static <Data> Builder<Data> builder() {
    return new Builder<>();
  }



}
