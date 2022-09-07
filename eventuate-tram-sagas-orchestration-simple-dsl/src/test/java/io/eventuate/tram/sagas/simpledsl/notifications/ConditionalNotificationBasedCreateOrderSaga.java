package io.eventuate.tram.sagas.simpledsl.notifications;

import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

public class ConditionalNotificationBasedCreateOrderSaga implements SimpleSaga<ConditionalNotificationBasedCreateOrderSagaData> {

  private final SagaDefinition<ConditionalNotificationBasedCreateOrderSagaData> sagaDefinition;

  public ConditionalNotificationBasedCreateOrderSaga(ConditionalNotificationBasedCreateOrderSagaSteps steps) {
    this.sagaDefinition =
            step()
                  .invokeLocal(steps::createOrder)
                  .withCompensation(steps::rejectOrder)
            .step()
                  .invokeParticipant(ConditionalNotificationBasedCreateOrderSagaData::reserveCredit)
                  .withCompensation(ConditionalNotificationBasedCreateOrderSagaData::releaseCredit)
            .step()
                    .invokeParticipant(ConditionalNotificationBasedCreateOrderSagaData::reserveInventory)
                    .withCompensationNotification(ConditionalNotificationBasedCreateOrderSagaData::isReleaseInventory, ConditionalNotificationBasedCreateOrderSagaData::releaseInventory)
            .step()
                  .invokeLocal(steps::approveOrder)
            .step()
                  .notifyParticipant(ConditionalNotificationBasedCreateOrderSagaData::isFulfillOrder, ConditionalNotificationBasedCreateOrderSagaData::fulfillOrder)
            .build();
  }


  @Override
  public SagaDefinition<ConditionalNotificationBasedCreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
