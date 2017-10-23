package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.events.common.DomainEvent;

import java.util.List;

public class EventToPublish {
  private final Class aggregateType;
  private final String aggregateId;
  private final List<DomainEvent> domainEvents;

  public EventToPublish(Class aggregateType, String aggregateId, List<DomainEvent> domainEvents) {
    this.aggregateType = aggregateType;
    this.aggregateId = aggregateId;
    this.domainEvents = domainEvents;
  }

  public Class getAggregateType() {
    return aggregateType;
  }

  public String getAggregateId() {
    return aggregateId;
  }

  public List<DomainEvent> getDomainEvents() {
    return domainEvents;
  }
}
