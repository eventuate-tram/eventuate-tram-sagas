package io.eventuate.tram.sagas;

import io.eventuate.javaclient.spring.jdbc.EventuateSchema;
import io.eventuate.tram.sagas.orchestration.AggregateInstanceSubscriptionsDAO;

public class AggregateInstanceSubscriptionsDAODefaultSchemaTest extends AggregateInstanceSubscriptionsDAOSchemaTest {

  @Override
  protected AggregateInstanceSubscriptionsDAO getAggregateInstanceSubscriptionsDAO() {
    return new AggregateInstanceSubscriptionsDAO();
  }

  @Override
  protected String getExpectedInsert() {
    return String.format("INSERT INTO %s.aggregate_instance_subscriptions(aggregate_id, event_type, saga_type, saga_id) values(?, ?, ?, ?)", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedSelect() {
    return String.format("select saga_type, saga_id from %s.aggregate_instance_subscriptions where aggregate_id = ? and event_type = ?", EventuateSchema.DEFAULT_SCHEMA);
  }

  @Override
  protected String getExpectedDelete() {
    return String.format("DELETE FROM %s.aggregate_instance_subscriptions WHERE saga_type = ? AND saga_id =?", EventuateSchema.DEFAULT_SCHEMA);
  }
}
