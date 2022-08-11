package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;

public class ReactiveLocalSaga implements SimpleReactiveSaga<LocalSagaData> {

  private ReactiveSagaDefinition<LocalSagaData> sagaDefinition;

  public ReactiveLocalSaga(LocalSagaSteps steps) {
    this.sagaDefinition =
            step()
                    .invokeLocal(steps::localStep1)
                    .withCompensation(steps::localStep1Compensation)
                    .step()
                    .invokeParticipant(LocalSagaData::do2)
                    .withCompensation(LocalSagaData::undo2)
                    .step()
                    .invokeLocal(steps::localStep3)
                    .build();
  }


  @Override
  public ReactiveSagaDefinition<LocalSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
