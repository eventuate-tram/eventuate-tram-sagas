package io.eventuate.tram.sagas.common.spring;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.common.SagaLockManagerImpl;
import io.eventuate.tram.sagas.inmemory.TramSagaInMemoryConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableAutoConfiguration
@Import({TramSagaInMemoryConfiguration.class})
public class SagaLockManagerIntegrationTestConfiguration {

  @Bean
  public SagaLockManager sagaLockManager(JdbcTemplate jdbcTemplate, EventuateSchema eventuateSchema) {
    return new SagaLockManagerImpl(jdbcTemplate, eventuateSchema);
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }

}
