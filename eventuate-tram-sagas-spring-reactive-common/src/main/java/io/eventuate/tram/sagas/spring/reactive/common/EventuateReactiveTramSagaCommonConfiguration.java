package io.eventuate.tram.sagas.spring.reactive.common;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveDatabaseConfiguration;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateCommonReactiveDatabaseConfiguration.class)
public class EventuateReactiveTramSagaCommonConfiguration {

  @Bean
  public ReactiveSagaLockManager sagaLockManager(EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                 EventuateSchema eventuateSchema) {
    return new ReactiveSagaLockManagerImpl(eventuateJdbcStatementExecutor, eventuateSchema);
  }
}