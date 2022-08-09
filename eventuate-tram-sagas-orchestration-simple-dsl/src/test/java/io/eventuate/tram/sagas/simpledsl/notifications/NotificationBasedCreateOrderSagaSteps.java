package io.eventuate.tram.sagas.simpledsl.notifications;

public interface NotificationBasedCreateOrderSagaSteps {

  void createOrder(NotificationBasedCreateOrderSagaData data);
  void rejectOrder(NotificationBasedCreateOrderSagaData data);
  void approveOrder(NotificationBasedCreateOrderSagaData data);

}
