package io.eventuate.tram.sagas.simpledsl;

public interface LocalSagaSteps {

  void localStep1(LocalSagaData data);
  void localStep1Compensation(LocalSagaData data);
  void localStep3(LocalSagaData data);

}
