package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.List;
import java.util.Optional;

public class SimpleSagaDefinition<Data>
        extends AbstractSimpleSagaDefinition<Data, SagaStep<Data>, StepToExecute<Data>>
        implements SagaDefinition<Data> {


  public SimpleSagaDefinition(List<SagaStep<Data>> steps) {
    super(steps);
  }

  @Override
  public SagaActions<Data> start(Data sagaData) {
    SagaExecutionState currentState = new SagaExecutionState(-1, false);

    Optional<StepToExecute<Data>> stepToExecute = nextStepToExecute(currentState, sagaData);

    if (!stepToExecute.isPresent()) {
      return makeEndStateSagaActions(currentState);
    } else
      return stepToExecute.get().executeStep(sagaData, currentState);
  }

  @Override
  public SagaActions<Data> handleReply(String sagaType, String sagaId, String currentState, Data sagaData, Message message) {

    SagaExecutionState state = SagaExecutionStateJsonSerde.decodeState(currentState);
    SagaStep<Data> currentStep = steps.get(state.getCurrentlyExecuting());
    boolean compensating = state.isCompensating();

    currentStep.getReplyHandler(message, compensating).ifPresent(handler -> invokeReplyHandler(message, sagaData, (d, m) -> {
      handler.accept(d, m);
      return null;
    }));

    if (currentStep.isSuccessfulReply(compensating, message)) {
      return executeNextStep(sagaData, state);
    } else if (compensating) {
      return handleFailedCompensatingTransaction(sagaType, sagaId, state, message);
    } else {
      return executeNextStep(sagaData, state.startCompensating());
    }
  }


  protected SagaActions<Data> executeNextStep(Data data, SagaExecutionState state) {
    Optional<StepToExecute<Data>> stepToExecute = nextStepToExecute(state, data);
    if (!stepToExecute.isPresent()) {
      return makeEndStateSagaActions(state);
    } else {
      // do something
      return stepToExecute.get().executeStep(data, state);
    }
  }


  @Override
  protected StepToExecute<Data> makeStepToExecute(int skipped, boolean compensating, SagaStep<Data> step) {
      return new StepToExecute<>(step, skipped, compensating);
  }

}
