package io.eventuate.tram.sagas.eventsourcingsupport;

import io.eventuate.Event;

import java.util.Map;

public class SagaReplyRequestedEvent implements Event {
  private Map<String, String> correlationHeaders;

  private SagaReplyRequestedEvent() {
  }

  public SagaReplyRequestedEvent(Map<String, String> correlationHeaders) {
    this.correlationHeaders = correlationHeaders;
  }

  public Map<String, String> getCorrelationHeaders() {
    return correlationHeaders;
  }

  public void setCorrelationHeaders(Map<String, String> correlationHeaders) {
    this.correlationHeaders = correlationHeaders;
  }


}
