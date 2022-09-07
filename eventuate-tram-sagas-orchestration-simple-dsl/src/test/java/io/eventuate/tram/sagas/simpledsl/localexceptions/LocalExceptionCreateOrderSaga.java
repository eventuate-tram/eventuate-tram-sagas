package io.eventuate.tram.sagas.simpledsl.localexceptions;

import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

public class LocalExceptionCreateOrderSaga implements SimpleSaga<LocalExceptionCreateOrderSagaData> {

  private final SagaDefinition<LocalExceptionCreateOrderSagaData> sagaDefinition;

  public LocalExceptionCreateOrderSaga(LocalExceptionCreateOrderSagaSteps steps) {
    this.sagaDefinition =
            step()
                  .invokeLocal(steps::createOrder)
                  .withCompensation(steps::rejectOrder)
            .step()
                  .invokeParticipant(LocalExceptionCreateOrderSagaData::reserveCredit)
                  .withCompensation(LocalExceptionCreateOrderSagaData::releaseCredit)
            .step()
                    .invokeParticipant(LocalExceptionCreateOrderSagaData::reserveInventory)
                    .withCompensationNotification(LocalExceptionCreateOrderSagaData::releaseInventory)
            .step()
                  .invokeLocal(steps::approveOrder)
                    .onException(InvalidOrderException.class, LocalExceptionCreateOrderSagaData::saveInvalidOrder)
                    .onExceptionRollback(InvalidOrderException.class)
            .step()
                  .notifyParticipant(LocalExceptionCreateOrderSagaData::fulfillOrder)
            .build();
  }


  @Override
  public SagaDefinition<LocalExceptionCreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
