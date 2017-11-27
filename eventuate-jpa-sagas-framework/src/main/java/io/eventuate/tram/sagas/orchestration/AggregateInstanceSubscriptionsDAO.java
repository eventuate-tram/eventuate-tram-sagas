package io.eventuate.tram.sagas.orchestration;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class AggregateInstanceSubscriptionsDAO {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private String deleteSql;
  private String insertSql;
  private String selectSql;

  public AggregateInstanceSubscriptionsDAO(EventuateSchema eventuateSchema) {
    String aggregateInstanceSubscriptionsTable = eventuateSchema.qualifyTable("aggregate_instance_subscriptions");

    deleteSql = String.format("DELETE FROM %s WHERE saga_type = ? AND saga_id =?",
            aggregateInstanceSubscriptionsTable);

    insertSql = String.format("INSERT INTO %s(aggregate_id, event_type, saga_type, saga_id) values(?, ?, ?, ?)",
            aggregateInstanceSubscriptionsTable);

    selectSql = String.format("select saga_type, saga_id from %s where aggregate_id = ? and event_type = ?",
            aggregateInstanceSubscriptionsTable);
  }

  public AggregateInstanceSubscriptionsDAO() {
    this(new EventuateSchema());
  }

  public String getDeleteSql() {
    return deleteSql;
  }

  public void setDeleteSql(String deleteSql) {
    this.deleteSql = deleteSql;
  }

  public String getInsertSql() {
    return insertSql;
  }

  public void setInsertSql(String insertSql) {
    this.insertSql = insertSql;
  }

  public String getSelectSql() {
    return selectSql;
  }

  public void setSelectSql(String selectSql) {
    this.selectSql = selectSql;
  }

  public void update(String sagaType, String sagaId, List<EventClassAndAggregateId> eventHandlers) {
    jdbcTemplate.update(deleteSql, sagaType, sagaId);
    for  (EventClassAndAggregateId eventClassAndAggregateId : eventHandlers) {
      jdbcTemplate.update(insertSql,
              Long.toString(eventClassAndAggregateId.getAggregateId()),
              eventClassAndAggregateId.getEventClass().getName(),
              sagaType,
              sagaId);
    }
  }

  public List<SagaTypeAndId> findSagas(String aggregateType, String aggregateId, String eventType) {
    return jdbcTemplate.query(selectSql,
            (rs, rowNum) -> new SagaTypeAndId(rs.getString("saga_type"), rs.getString("saga_Id")),
            aggregateId,
            eventType
            );
  }
}
