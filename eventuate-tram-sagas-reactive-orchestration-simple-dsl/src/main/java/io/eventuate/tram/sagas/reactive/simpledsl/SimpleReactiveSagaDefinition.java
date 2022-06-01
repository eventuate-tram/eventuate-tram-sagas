package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;
import io.eventuate.tram.sagas.simpledsl.AbstractSimpleSagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionState;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionStateJsonSerde;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class SimpleReactiveSagaDefinition<Data>
        extends AbstractSimpleSagaDefinition<Data, ReactiveSagaStep<Data>, ReactiveStepToExecute<Data>>
          implements ReactiveSagaDefinition<Data> {


  public SimpleReactiveSagaDefinition(List<ReactiveSagaStep<Data>> sagaSteps) {
    super(sagaSteps);
  }

  @Override
  public Publisher<SagaActions<Data>> start(Data sagaData) {
    SagaExecutionState currentState = new SagaExecutionState(-1, false);

    Optional<ReactiveStepToExecute<Data>> stepToExecute = nextStepToExecute(currentState, sagaData);

    if (!stepToExecute.isPresent()) {
      return Mono.just(makeEndStateSagaActions(currentState));
    } else {
      return stepToExecute.get().executeStep(sagaData, currentState);
    }
  }

  @Override
  public Publisher<SagaActions<Data>> handleReply(String sagaType, String sagaId, String currentState, Data sagaData, Message message) {

    SagaExecutionState state = SagaExecutionStateJsonSerde.decodeState(currentState);
    ReactiveSagaStep<Data> currentStep = steps.get(state.getCurrentlyExecuting());
    boolean compensating = state.isCompensating();

    return currentStep
            .getReplyHandler(message, compensating)
            .map(handler -> {
              Publisher<?> source = invokeReplyHandler(message, sagaData, handler);
              return Mono.from(source);
            })
            .orElse(Mono.empty())
            .then(Mono.defer(() -> {
              if (currentStep.isSuccessfulReply(compensating, message)) {
                return Mono.from(executeNextStep(sagaData, state));
              } else if (compensating) {
                return Mono.just(handleFailedCompensatingTransaction(sagaType, sagaId, state, message));
              } else {
                return Mono.from(executeNextStep(sagaData, state.startCompensating()));
              }
            }));
  }


  @Override
  protected ReactiveStepToExecute<Data> makeStepToExecute(int skipped, boolean compensating, ReactiveSagaStep<Data> step) {
    return new ReactiveStepToExecute<>(step, skipped, compensating) ;
  }

  private Publisher<SagaActions<Data>> executeNextStep(Data data, SagaExecutionState state) {
    Optional<ReactiveStepToExecute<Data>> stepToExecute = nextStepToExecute(state, data);
    if (!stepToExecute.isPresent()) {
      return Mono.just(makeEndStateSagaActions(state));
    } else {
      // do something
      return stepToExecute.get().executeStep(data, state);
    }
  }

}
