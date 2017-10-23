package io.eventuate.tram.sagas.orchestration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class AggregateInstanceSubscriptionsDAO {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public void update(String sagaType, String sagaId, List<EventClassAndAggregateId> eventHandlers) {
    jdbcTemplate.update("DELETE FROM aggregate_instance_subscriptions WHERE saga_type = ? AND saga_id =?", sagaType, sagaId);
    for  (EventClassAndAggregateId eventClassAndAggregateId : eventHandlers) {
      jdbcTemplate.update("INSERT INTO aggregate_instance_subscriptions(aggregate_id, event_type, saga_type, saga_id) values(?, ?, ?, ?)",
              Long.toString(eventClassAndAggregateId.getAggregateId()),
              eventClassAndAggregateId.getEventClass().getName(),
              sagaType,
              sagaId);
    }
  }

  public List<SagaTypeAndId> findSagas(String aggregateType, String aggregateId, String eventType) {
    return jdbcTemplate.query("Select saga_type, saga_id from aggregate_instance_subscriptions where aggregate_id = ? and event_type = ?",
            (rs, rowNum) -> new SagaTypeAndId(rs.getString("saga_type"), rs.getString("saga_Id")),
            aggregateId,
            eventType
            );
  }
}
