package io.eventuate.tram.sagas.simpledsl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SagaExecutionState {

    private final int currentlyExecuting;
    private final boolean compensating;
    private final boolean endState;

    @JsonCreator
    public SagaExecutionState(@JsonProperty("currentlyExecuting") final int currentlyExecuting,
                              @JsonProperty("compensating") final boolean compensating,
                              @JsonProperty("endState") final boolean endState) {
        this.currentlyExecuting = currentlyExecuting;
        this.compensating = compensating;
        this.endState = endState;
    }

    @JsonProperty("currentlyExecuting")
    public int getCurrentlyExecuting() {
        return currentlyExecuting;
    }

    @JsonProperty("compensating")
    public boolean isCompensating() {
        return compensating;
    }

    @JsonProperty("endState")
    public boolean isEndState() {
        return endState;
    }

    public SagaExecutionState startCompensating() {
        return new SagaExecutionState(currentlyExecuting, true, false);
    }

    public SagaExecutionState nextState(final int size) {
        return new SagaExecutionState(compensating ? currentlyExecuting - size : currentlyExecuting + size, compensating, false);
    }

    public static SagaExecutionState makeEndState() {
        return new SagaExecutionState(-1, false, true);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
