package io.eventuate.tram.sagas.spring.common;

import io.eventuate.common.database.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.database.jdbc.EventuateSchema;
import io.eventuate.common.database.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.common.SagaLockManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateCommonJdbcOperationsConfiguration.class)
public class EventuateTramSagaCommonConfiguration {

  @Bean
  public SagaLockManager sagaLockManager(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                         EventuateSchema eventuateSchema) {
    return new SagaLockManagerImpl(eventuateJdbcStatementExecutor, eventuateSchema);
  }
}