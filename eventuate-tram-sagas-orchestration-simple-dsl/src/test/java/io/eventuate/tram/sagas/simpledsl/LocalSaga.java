package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

public class LocalSaga implements SimpleSaga<LocalSagaData> {

  private SagaDefinition<SagaActions<LocalSagaData>, LocalSagaData> sagaDefinition;

  public LocalSaga(LocalSagaSteps steps) {
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
  public SagaDefinition<SagaActions<LocalSagaData>, LocalSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
