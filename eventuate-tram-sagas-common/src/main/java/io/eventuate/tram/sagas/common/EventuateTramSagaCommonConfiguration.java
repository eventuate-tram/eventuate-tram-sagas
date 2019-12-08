package io.eventuate.tram.sagas.common;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateSchemaConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateSchemaConfiguration.class)
public class EventuateTramSagaCommonConfiguration {

  @Bean
  public SagaLockManager sagaLockManager(EventuateSchema eventuateSchema) {
    return new SagaLockManagerImpl(eventuateSchema);
  }

}
