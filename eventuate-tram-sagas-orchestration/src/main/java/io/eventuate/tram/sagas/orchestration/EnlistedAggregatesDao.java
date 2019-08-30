package io.eventuate.tram.sagas.orchestration;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;

public class EnlistedAggregatesDao {

  private JdbcTemplate jdbcTemplate;

  public EnlistedAggregatesDao(JdbcTemplate jdbcTemplate) {
      this.jdbcTemplate = jdbcTemplate;
  }

  public void save(String sagaId, Set<EnlistedAggregate> enlistedAggregates) {
    for (EnlistedAggregate ela : enlistedAggregates) {
      try {
        jdbcTemplate.update("INSERT INTO saga_enlisted_aggregates(saga_id, aggregate_type, aggregate_id) values(?,?,?)",
                sagaId,
                ela.getAggregateClass(),
                ela.getAggregateId());
      } catch (DuplicateKeyException e) {
        // ignore
      }
    }
  }

  public Set<EnlistedAggregate> findEnlistedAggregates(String sagaId) {
    return new HashSet<>(jdbcTemplate.query("Select aggregate_type, aggregate_id from saga_enlisted_aggregates where saga_id = ?",
            (rs, rowNum) -> {
              try {
                return new EnlistedAggregate((Class) ClassUtils.forName(rs.getString("aggregate_type"), getClass().getClassLoader()), rs.getString("aggregate_id"));
              } catch (ClassNotFoundException e) {
                throw new RuntimeException();
              }
            },
            sagaId));
  }

  public Set<String> findSagas(Class aggregateType, String aggregateId) {
    return new HashSet<>(jdbcTemplate.query("Select saga_id from saga_enlisted_aggregates where aggregate_type = ? AND  aggregate_id = ?",
            (rs, rowNum) -> {
              return rs.getString("aggregate_type");
            },
            aggregateType, aggregateId));
  }
}
