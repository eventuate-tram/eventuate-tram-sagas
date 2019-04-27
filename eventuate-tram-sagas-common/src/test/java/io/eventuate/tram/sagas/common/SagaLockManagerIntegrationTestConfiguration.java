package io.eventuate.tram.sagas.common;

import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.common.SagaLockManagerImpl;
import io.eventuate.tram.sagas.inmemory.TramSagaInMemoryConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramSagaInMemoryConfiguration.class})
public class SagaLockManagerIntegrationTestConfiguration {

  @Bean
  public SagaLockManager sagaLockManager() {
    return new SagaLockManagerImpl();
  }

  @Bean
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }

}
