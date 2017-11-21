package io.eventuate.tram.sagas.orchestration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class AggregateInstanceSubscriptionsDAO {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private String aggregateInstanceSubscriptionsTable;

  public AggregateInstanceSubscriptionsDAO() {
    aggregateInstanceSubscriptionsTable = "eventuate.aggregate_instance_subscriptions";
  }

  public void update(String sagaType, String sagaId, List<EventClassAndAggregateId> eventHandlers) {
    jdbcTemplate.update(String.format("DELETE FROM %s WHERE saga_type = ? AND saga_id =?", aggregateInstanceSubscriptionsTable), sagaType, sagaId);
    for  (EventClassAndAggregateId eventClassAndAggregateId : eventHandlers) {
      jdbcTemplate.update(String.format("INSERT INTO %s(aggregate_id, event_type, saga_type, saga_id) values(?, ?, ?, ?)", aggregateInstanceSubscriptionsTable),
              Long.toString(eventClassAndAggregateId.getAggregateId()),
              eventClassAndAggregateId.getEventClass().getName(),
              sagaType,
              sagaId);
    }
  }

  public List<SagaTypeAndId> findSagas(String aggregateType, String aggregateId, String eventType) {
    return jdbcTemplate.query(String.format("select saga_type, saga_id from %s where aggregate_id = ? and event_type = ?", aggregateInstanceSubscriptionsTable),
            (rs, rowNum) -> new SagaTypeAndId(rs.getString("saga_type"), rs.getString("saga_Id")),
            aggregateId,
            eventType
            );
  }
}
