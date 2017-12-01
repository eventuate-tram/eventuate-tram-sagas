package io.eventuate.tram.sagas;

import io.eventuate.tram.sagas.orchestration.AggregateInstanceSubscriptionsDAO;
import org.junit.Assert;
import org.junit.Test;

public abstract class AggregateInstanceSubscriptionsDAOSchemaTest {

  @Test
  public void testInsert() {
      Assert.assertEquals(getExpectedInsert(), getAggregateInstanceSubscriptionsDAO().getInsertSql());
  }

  @Test
  public void testSelect() {
    Assert.assertEquals(getExpectedSelect(), getAggregateInstanceSubscriptionsDAO().getSelectSql());
  }

  @Test
  public void testDelete() {
    Assert.assertEquals(getExpectedDelete(), getAggregateInstanceSubscriptionsDAO().getDeleteSql());
  }

  protected abstract AggregateInstanceSubscriptionsDAO getAggregateInstanceSubscriptionsDAO();

  protected abstract String getExpectedInsert();
  protected abstract String getExpectedSelect();
  protected abstract String getExpectedDelete();
}
