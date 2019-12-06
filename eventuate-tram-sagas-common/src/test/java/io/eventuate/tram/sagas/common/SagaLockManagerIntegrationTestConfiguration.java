package io.eventuate.tram.sagas.common;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateSchemaConfiguration;
import io.eventuate.tram.sagas.inmemory.TramSagaInMemoryConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramSagaInMemoryConfiguration.class, EventuateSchemaConfiguration.class})
public class SagaLockManagerIntegrationTestConfiguration {

  @Bean
  public SagaLockManager sagaLockManager(EventuateSchema eventuateSchema) {
    return new SagaLockManagerImpl(eventuateSchema);
  }


}
