package io.eventuate.tram.sagas.common.micronaut;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.common.SagaLockManagerImpl;
import io.micronaut.context.annotation.Factory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Singleton;

@Factory
public class SagaLockManagerIntegrationTestFactory {

  @Singleton
  public SagaLockManager sagaLockManager(JdbcTemplate jdbcTemplate, EventuateSchema eventuateSchema) {
    return new SagaLockManagerImpl(jdbcTemplate, eventuateSchema);
  }

  @Singleton
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
