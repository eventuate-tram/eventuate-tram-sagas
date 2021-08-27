package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionState;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static io.eventuate.tram.sagas.simpledsl.SagaExecutionStateJsonSerde.encodeState;

public class ReactiveStepToExecute<Data> {
  private final Optional<ReactiveSagaStep<Data>> step;
  private final int skipped;
  private final boolean compensating;


  public ReactiveStepToExecute(Optional<ReactiveSagaStep<Data>> step, int skipped, boolean compensating) {
    this.compensating = compensating;
    this.step = step;
    this.skipped = skipped;
  }


  private int size() {
    return step.map(x -> 1).orElse(0) + skipped;
  }

  public boolean isEmpty() {
    return !step.isPresent();
  }

  public Publisher<SagaActions<Data>> executeStep(Data data, SagaExecutionState currentState) {
    SagaExecutionState newState = currentState.nextState(size());
    SagaActions.Builder<Data> builder = SagaActions.builder();
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
