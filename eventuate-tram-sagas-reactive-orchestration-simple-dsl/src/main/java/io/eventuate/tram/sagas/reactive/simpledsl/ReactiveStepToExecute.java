package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.simpledsl.AbstractStepToExecute;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionState;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class ReactiveStepToExecute<Data> extends AbstractStepToExecute<Data, ReactiveSagaStep<Data>> {


  public ReactiveStepToExecute(ReactiveSagaStep<Data> step, int skipped, boolean compensating) {
    super(step, skipped, compensating);
  }


  public Publisher<SagaActions<Data>> executeStep(Data data, SagaExecutionState currentState) {
    SagaExecutionState newState = currentState.nextState(size());
    SagaActions.Builder<Data> builder = SagaActions.builder();
    boolean compensating = currentState.isCompensating();

    return Mono
            .from(step.makeStepOutcome(data, this.compensating))
            .map(outcome -> {
              outcome.visit(builder::withIsLocal, builder::withCommands);
              return outcome;
            })
            .then(Mono.fromSupplier(() ->
                    makeSagaActions(builder, data, newState, compensating)));
  }


}
