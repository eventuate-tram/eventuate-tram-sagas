package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.simpledsl.AbstractStepToExecute;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionState;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static io.eventuate.tram.sagas.simpledsl.SagaExecutionStateJsonSerde.encodeState;

public class ReactiveStepToExecute<DATA> extends AbstractStepToExecute<ReactiveSagaStep<DATA>, Publisher<SagaActions<DATA>>, DATA> {
  public ReactiveStepToExecute(Optional<ReactiveSagaStep<DATA>> step, int skipped, boolean compensating) {
    super(step, skipped, compensating);
  }

  @Override
  public Publisher<SagaActions<DATA>> executeStep(DATA data, SagaExecutionState currentState) {
    SagaExecutionState newState = currentState.nextState(size());
    SagaActions.Builder<DATA> builder = SagaActions.builder();
    boolean compensating = currentState.isCompensating();

    return Mono
            .from(step.get().makeStepOutcome(data, this.compensating))
            .map(step -> {
              step.visit(builder::withIsLocal, builder::withCommands);
              return step;
            })
            .then(Mono.fromSupplier(() ->
              builder
                      .withUpdatedSagaData(data)
                      .withUpdatedState(encodeState(newState))
                      .withIsEndState(newState.isEndState())
                      .withIsCompensating(compensating)
                      .build()));
  }

}
