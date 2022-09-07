package io.eventuate.tram.sagas.simpledsl.notifications;

public interface ConditionalNotificationBasedCreateOrderSagaSteps {

  void createOrder(ConditionalNotificationBasedCreateOrderSagaData data);
  void rejectOrder(ConditionalNotificationBasedCreateOrderSagaData data);
  void approveOrder(ConditionalNotificationBasedCreateOrderSagaData data);

}
