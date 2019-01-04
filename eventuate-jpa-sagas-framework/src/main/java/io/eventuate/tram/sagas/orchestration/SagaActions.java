package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SagaActions<Data> {

    private final List<CommandWithDestination> commands;
    private final Data updatedSagaData;
    private final String updatedState;
    private boolean endState;
    private boolean compensating;

    public SagaActions(final List<CommandWithDestination> commands,
                       final Data updatedSagaData,
                       final String updatedState,
                       final boolean endState,
                       final boolean compensating) {
        this.commands = Objects.requireNonNull(commands);
        this.updatedSagaData = updatedSagaData;
        this.updatedState = updatedState;
        this.endState = endState;
        this.compensating = compensating;
    }

    public List<CommandWithDestination> getCommands() {
        return commands;
    }

    public Optional<Data> getUpdatedSagaData() {
        return Optional.ofNullable(updatedSagaData);
    }

    public Optional<String> getUpdatedState() {
        return Optional.ofNullable(updatedState);
    }

    public boolean isEndState() {
        return endState;
    }

    public boolean isCompensating() {
        return compensating;
    }

    public static <Data> Builder<Data> builder() {
        return new Builder<>();
    }

    public static class Builder<Data> {
        private List<CommandWithDestination> commands = new ArrayList<>();
        private Data updatedSagaData;
        private String updatedState;
        private boolean endState;

        private boolean compensating;

        public Builder() {
        }

        public SagaActions<Data> build() {
            return new SagaActions<>(commands, updatedSagaData, updatedState, endState, compensating);
        }

        public Builder<Data> withCommand(final CommandWithDestination command) {
            commands.add(command);
            return this;
        }

        public Builder<Data> withUpdatedSagaData(final Data data) {
            this.updatedSagaData = data;
            return this;
        }

        public Builder<Data> withUpdatedState(final String state) {
            this.updatedState = state;
            return this;
        }

        public Builder<Data> withCommands(final List<CommandWithDestination> commands) {
            this.commands.addAll(commands);
            return this;
        }

        public Builder<Data> withIsEndState(final boolean endState) {
            this.endState = endState;
            return this;
        }

        public Builder<Data> withIsCompensating(final boolean compensating) {
            this.compensating = compensating;
            return this;
        }

    }

}
