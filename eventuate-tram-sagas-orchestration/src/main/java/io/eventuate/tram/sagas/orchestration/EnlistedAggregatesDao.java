package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import org.apache.commons.lang.ClassUtils;

import java.util.HashSet;
import java.util.Set;

public class EnlistedAggregatesDao {

  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;

  public EnlistedAggregatesDao(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor) {
      this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;
  }

  public void save(String sagaId, Set<EnlistedAggregate> enlistedAggregates) {
    for (EnlistedAggregate ela : enlistedAggregates) {
      try {
        eventuateJdbcStatementExecutor.update("INSERT INTO saga_enlisted_aggregates(saga_id, aggregate_type, aggregate_id) values(?,?,?)",
                sagaId,
                ela.getAggregateClass(),
                ela.getAggregateId());
      } catch (EventuateDuplicateKeyException e) {
        // ignore
      }
    }
  }

  public Set<EnlistedAggregate> findEnlistedAggregates(String sagaId) {
    return new HashSet<>(eventuateJdbcStatementExecutor.query("Select aggregate_type, aggregate_id from saga_enlisted_aggregates where saga_id = ?",
            (rs, rowNum) -> {
              try {
                return new EnlistedAggregate(ClassUtils.getClass(rs.getString("aggregate_type")), rs.getString("aggregate_id"));
              } catch (ClassNotFoundException e) {
                throw new RuntimeException();
              }
            },
            sagaId));
  }

  public Set<String> findSagas(Class aggregateType, String aggregateId) {
    return new HashSet<>(eventuateJdbcStatementExecutor.query("Select saga_id from saga_enlisted_aggregates where aggregate_type = ? AND  aggregate_id = ?",
            (rs, rowNum) -> rs.getString("aggregate_type"),
            aggregateType, aggregateId));
  }
}
