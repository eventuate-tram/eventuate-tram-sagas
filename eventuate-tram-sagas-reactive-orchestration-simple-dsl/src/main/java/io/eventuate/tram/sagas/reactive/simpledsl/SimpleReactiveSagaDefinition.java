package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.simpledsl.AbstractSagaDefinition;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.simpledsl.AbstractStepToExecute;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionState;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class SimpleReactiveSagaDefinition<SAGA_DATA>
        extends AbstractSagaDefinition<ReactiveSagaStep<SAGA_DATA>, Publisher<SagaActions<SAGA_DATA>>, SAGA_DATA> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  public SimpleReactiveSagaDefinition(List<ReactiveSagaStep<SAGA_DATA>> sagaSteps) {
    super(sagaSteps);
  }

  @Override
  protected AbstractStepToExecute<ReactiveSagaStep<SAGA_DATA>, Publisher<SagaActions<SAGA_DATA>>, SAGA_DATA> createStepToExecute(Optional<ReactiveSagaStep<SAGA_DATA>> step, int skipped, boolean compensating) {
    return new ReactiveStepToExecute<>(step, skipped, compensating);
  }

  @Override
  protected Publisher<SagaActions<SAGA_DATA>> actualSagaActionsFromPureSagaActions(SagaActions<SAGA_DATA> sagaActions) {
    return Mono.just(sagaActions);
  }

  @Override
  protected Publisher<SagaActions<SAGA_DATA>> executeReplyStep(ReactiveSagaStep<SAGA_DATA> currentStep,
                                                               SagaExecutionState state,
                                                               Message message,
                                                               SAGA_DATA sagaData,
                                                               boolean compensating) {
    return currentStep
            .getReplyHandler(message, compensating)
            .map(handler -> Mono.from(handler.apply(sagaData, prepareReply(message))))
            .orElse(Mono.empty())
            .then(Mono.defer(() -> {
              if (currentStep.isSuccessfulReply(compensating, message)) {
                return Mono.from(executeNextStep(sagaData, state));
              } else if (compensating) {
                return Mono.error(new UnsupportedOperationException("Failure when compensating"));
              } else {
                return Mono.from(executeNextStep(sagaData, state.startCompensating()));
              }
            }));
  }
}
