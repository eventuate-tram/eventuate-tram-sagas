package io.eventuate.tram.sagas.simpledsl.notifications;

import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

public class NotificationBasedCreateOrderSaga implements SimpleSaga<NotificationBasedCreateOrderSagaData> {

  private final SagaDefinition<NotificationBasedCreateOrderSagaData> sagaDefinition;

  public NotificationBasedCreateOrderSaga(NotificationBasedCreateOrderSagaSteps steps) {
    this.sagaDefinition =
            step()
                  .invokeLocal(steps::createOrder)
                  .withCompensation(steps::rejectOrder)
            .step()
                  .invokeParticipant(NotificationBasedCreateOrderSagaData::reserveCredit)
                  .withCompensation(NotificationBasedCreateOrderSagaData::releaseCredit)
            .step()
                    .invokeParticipant(NotificationBasedCreateOrderSagaData::reserveInventory)
                    .withCompensationNotification(NotificationBasedCreateOrderSagaData::releaseInventory)
            .step()
                  .invokeLocal(steps::approveOrder)
            .step()
                  .notifyParticipant(NotificationBasedCreateOrderSagaData::fulfillOrder)
            .build();
  }


  @Override
  public SagaDefinition<NotificationBasedCreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
