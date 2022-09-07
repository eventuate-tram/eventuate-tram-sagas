package io.eventuate.tram.sagas.simpledsl.localexceptions;

public interface LocalExceptionCreateOrderSagaSteps {

  void createOrder(LocalExceptionCreateOrderSagaData data);
  void rejectOrder(LocalExceptionCreateOrderSagaData data);
  void approveOrder(LocalExceptionCreateOrderSagaData data);

}
