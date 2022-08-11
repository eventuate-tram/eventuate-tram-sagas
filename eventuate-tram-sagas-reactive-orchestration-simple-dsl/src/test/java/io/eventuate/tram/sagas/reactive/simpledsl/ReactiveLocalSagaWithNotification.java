package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;

public class ReactiveLocalSagaWithNotification implements SimpleReactiveSaga<LocalSagaData> {

  private ReactiveSagaDefinition<LocalSagaData> sagaDefinition;

  public ReactiveLocalSagaWithNotification(LocalSagaSteps steps) {
    this.sagaDefinition =
            step()
                    .invokeLocal(steps::localStep1)
                    .withCompensation(steps::localStep1Compensation)
                    .step()
                    .invokeParticipant(LocalSagaData::do2)
                    .withCompensationNotification(LocalSagaData::undo2)
                    .step()
                    .invokeLocal(steps::localStep3)
                    .step()
                    .notifyParticipant(LocalSagaData::notify3)
                    .build();
  }


  @Override
  public ReactiveSagaDefinition<LocalSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
