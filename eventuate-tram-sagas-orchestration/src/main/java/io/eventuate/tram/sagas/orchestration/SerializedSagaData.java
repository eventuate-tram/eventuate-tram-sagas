package io.eventuate.tram.sagas.orchestration;

public class SerializedSagaData {

  private String sagaDataType;
  private String sagaDataJSON;

  public SerializedSagaData(String sagaDataType, String sagaDataJSON) {
    this.sagaDataType = sagaDataType;
    this.sagaDataJSON = sagaDataJSON;
  }

  public String getSagaDataJSON() {
    return sagaDataJSON;
  }

  public String getSagaDataType() {
    return sagaDataType;
  }
}
