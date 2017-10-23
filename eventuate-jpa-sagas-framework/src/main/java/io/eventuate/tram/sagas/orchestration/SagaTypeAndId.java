package io.eventuate.tram.sagas.orchestration;

public class SagaTypeAndId {
  private final String sagaType;
  private final String sagaId;

  public SagaTypeAndId(String sagaType, String sagaId) {
    this.sagaType = sagaType;
    this.sagaId = sagaId;
  }

  public String getSagaId() {
    return sagaId;
  }

  public String getSagaType() {

    return sagaType;
  }
}
