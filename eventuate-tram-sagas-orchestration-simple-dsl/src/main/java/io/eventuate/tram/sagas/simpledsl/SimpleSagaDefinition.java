package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class SimpleSagaDefinition<SAGA_DATA> extends AbstractSagaDefinition<SagaStep<SAGA_DATA>, SagaActions<SAGA_DATA>, SAGA_DATA> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public SimpleSagaDefinition(List<SagaStep<SAGA_DATA>> sagaSteps) {
    super(sagaSteps);
  }

  @Override
  protected AbstractStepToExecute<SagaStep<SAGA_DATA>, SagaActions<SAGA_DATA>, SAGA_DATA> createStepToExecute(Optional<SagaStep<SAGA_DATA>> step,
                                                                                                              int skipped,
                                                                                                              boolean compensating) {
    return new StepToExecute<>(step, skipped, compensating);
  }

  @Override
  protected SagaActions<SAGA_DATA> actualSagaActionsFromPureSagaActions(SagaActions<SAGA_DATA> sagaActions) {
    return sagaActions;
  }

  @Override
  protected SagaActions<SAGA_DATA> executeReplyStep(SagaStep<SAGA_DATA> currentStep,
                                                    SagaExecutionState state,
                                                    Message message,
                                                    SAGA_DATA sagaData,
                                                    boolean compensating) {

    currentStep.getReplyHandler(message, compensating).ifPresent(handler -> {
      handler.accept(sagaData, prepareReply(message));
    });

    if (currentStep.isSuccessfulReply(compensating, message)) {
      return executeNextStep(sagaData, state);
    } else if (compensating) {
      throw new UnsupportedOperationException("Failure when compensating");
    } else {
      return executeNextStep(sagaData, state.startCompensating());
    }
  }
}
