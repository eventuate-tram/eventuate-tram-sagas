package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

public class ConditionalSaga implements SimpleSaga<ConditionalSagaData> {

  private SagaDefinition<SagaActions<ConditionalSagaData>, ConditionalSagaData> sagaDefinition =
          step()
              .invokeParticipant(ConditionalSagaData::isInvoke1, ConditionalSagaData::do1)
              .withCompensation(ConditionalSagaData::isInvoke1, ConditionalSagaData::undo1)
          .step()
              .invokeParticipant(ConditionalSagaData::do2)
          .build();


  @Override
  public SagaDefinition<SagaActions<ConditionalSagaData>, ConditionalSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
