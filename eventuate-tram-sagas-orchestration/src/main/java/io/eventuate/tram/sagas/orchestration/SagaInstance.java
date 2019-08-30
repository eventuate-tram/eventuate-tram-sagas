package io.eventuate.tram.sagas.orchestration;

import java.util.Set;

public class SagaInstance {

  private String sagaType;
  private String id;
  private String lastRequestId;
  private SerializedSagaData serializedSagaData;
  private String stateName;
  private Set<DestinationAndResource> destinationsAndResources;
  private Boolean endState = false;
  private Boolean compensating = false;

  public void setSagaType(String sagaType) {
    this.sagaType = sagaType;
  }

  public String getStateName() {
    return stateName;
  }

  public void setStateName(String stateName) {
    this.stateName = stateName;
  }

  public SagaInstance(String sagaType, String sagaId, String stateName, String lastRequestId, SerializedSagaData serializedSagaData, Set<DestinationAndResource> destinationsAndResources) {
    this.sagaType = sagaType;
    this.id = sagaId;
    this.stateName = stateName;
    this.lastRequestId = lastRequestId;
    this.serializedSagaData = serializedSagaData;
    this.destinationsAndResources = destinationsAndResources;
  }

  public SerializedSagaData getSerializedSagaData() {
    return serializedSagaData;
  }

  public void setSerializedSagaData(SerializedSagaData serializedSagaData) {
    this.serializedSagaData = serializedSagaData;
  }


  public void setId(String id) {
    this.id = id;
  }

  public String getSagaType() {
    return sagaType;
  }

  public String getId() {
    return id;
  }

  public String getLastRequestId() {
    return lastRequestId;
  }

  public void setLastRequestId(String requestId) {
    this.lastRequestId = requestId;
  }

  public void addDestinationsAndResources(Set<DestinationAndResource> destinationAndResources) {
    this.destinationsAndResources.addAll(destinationAndResources);
  }

  public Set<DestinationAndResource> getDestinationsAndResources() {
    return destinationsAndResources;
  }

  public void setEndState(Boolean endState) {
    this.endState = endState;
  }

  public Boolean isEndState() {
    return endState;
  }

  public void setCompensating(Boolean compensating) {
    this.compensating = compensating;
  }

  public Boolean isCompensating() {
    return compensating;
  }
}
