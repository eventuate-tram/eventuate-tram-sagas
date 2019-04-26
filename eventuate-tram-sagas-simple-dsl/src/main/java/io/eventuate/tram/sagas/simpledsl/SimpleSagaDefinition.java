package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SimpleSagaDefinition<Data> implements SagaDefinition<Data> {
  private List<SagaStep<Data>> sagaSteps;

  public SimpleSagaDefinition(List<SagaStep<Data>> sagaSteps) {
    this.sagaSteps = sagaSteps;
  }

  @Override
  public SagaActions<Data> start(Data sagaData) {
    SagaExecutionState currentState = new SagaExecutionState(-1, false);

    StepToExecute<Data> stepToExecute = nextStepToExecute(currentState, sagaData);

    if (stepToExecute.isEmpty()) {
      return makeEndStateSagaActions(currentState);
    } else
      return stepToExecute.executeStep(sagaData, currentState);
  }

  @Override
  public SagaActions<Data> handleReply(String currentState, Data sagaData, Message message) {

    SagaExecutionState state = SagaExecutionStateJsonSerde.decodeState(currentState);
    SagaStep<Data> currentStep = sagaSteps.get(state.getCurrentlyExecuting());
    boolean compensating = state.isCompensating();

    Optional<BiConsumer<Data, Object>> possibleReplyHandler = currentStep.getReplyHandler(message, compensating);
    if (currentStep.isSuccessfulReply(compensating, message)) {
      return handleReplyAndExecuteNextStep(message, sagaData, state, possibleReplyHandler);
    } else if (compensating) {
      throw new UnsupportedOperationException("Failure when compensating");
    } else {
      return handleReplyAndExecuteNextStep(message, sagaData, state.startCompensating(), possibleReplyHandler);
    }
  }



  private StepToExecute<Data> nextStepToExecute(SagaExecutionState state, Data data) {
    int skipped = 0;
    boolean compensating = state.isCompensating();
    int direction = compensating ? -1 : +1;
    for (int i = state.getCurrentlyExecuting() + direction; i >= 0 && i < sagaSteps.size(); i = i + direction) {
      SagaStep<Data> step = sagaSteps.get(i);
      if ((compensating ? step.hasCompensation(data) : step.hasAction(data))) {
        return new StepToExecute<>(Optional.of(step), skipped, compensating);
      } else
        skipped++;
    }
    return new StepToExecute<>(Optional.empty(), skipped, compensating);
  }

  private SagaActions<Data> handleReplyAndExecuteNextStep(Message message, Data data,
                                                          SagaExecutionState state,
                                                          Optional<BiConsumer<Data, Object>> possibleReplyHandler) {
    StepToExecute<Data> stepToExecute = nextStepToExecute(state, data);
    if (stepToExecute.isEmpty()) {
      return makeEndStateSagaActions(state);
    } else {
        Class m;
        try {
          m = Class.forName(message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }

        Object reply = JSonMapper.fromJson(message.getPayload(), m);
        possibleReplyHandler.ifPresent(handler -> handler.accept(data, reply));
        // do something
        return stepToExecute.executeStep(data, state);
    }
  }

  private SagaActions<Data> makeEndStateSagaActions(SagaExecutionState state) {
    return SagaActions.<Data>builder()
            .withUpdatedState(SagaExecutionStateJsonSerde.encodeState(SagaExecutionState.makeEndState()))
            .withIsEndState(true)
            .withIsCompensating(state.isCompensating())
            .build();
  }


}
