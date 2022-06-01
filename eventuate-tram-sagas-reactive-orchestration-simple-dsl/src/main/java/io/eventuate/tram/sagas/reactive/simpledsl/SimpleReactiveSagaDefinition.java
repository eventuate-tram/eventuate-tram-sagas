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

public class SimpleReactiveSagaDefinition<Data>
        extends AbstractSimpleSagaDefinition<Data, ReactiveSagaStep<Data>, ReactiveStepToExecute<Data>, ReactiveSagaActionsProvider<Data>>
          implements ReactiveSagaDefinition<Data> {


  public SimpleReactiveSagaDefinition(List<ReactiveSagaStep<Data>> sagaSteps) {
    super(sagaSteps);
  }

  @Override
  public Publisher<SagaActions<Data>> start(Data sagaData) {
    return toSagaActions(firstStepToExecute(sagaData));
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
              ReactiveSagaActionsProvider<Data> sap = sagaActionsForNextStep(sagaType, sagaId, sagaData, message, state, currentStep, compensating);
              return toSagaActions(sap);
            }));
  }

  private Mono<SagaActions<Data>> toSagaActions(ReactiveSagaActionsProvider<Data> sap) {
    return sap.getSagaActions() != null ? Mono.just(sap.getSagaActions()) : Mono.from(sap.getSagaActionsFunction().get());
  }

  @Override
  protected ReactiveStepToExecute<Data> makeStepToExecute(int skipped, boolean compensating, ReactiveSagaStep<Data> step) {
    return new ReactiveStepToExecute<>(step, skipped, compensating) ;
  }

  @Override
  protected ReactiveSagaActionsProvider<Data> makeSagaActionsProvider(SagaActions<Data> sagaActions) {
    return new ReactiveSagaActionsProvider<>(sagaActions);
  }

  @Override
  protected ReactiveSagaActionsProvider<Data> makeSagaActionsProvider(ReactiveStepToExecute<Data> stepToExecute, Data data, SagaExecutionState state) {
    return new ReactiveSagaActionsProvider<>(() -> stepToExecute.executeStep(data, state));
  }


}
