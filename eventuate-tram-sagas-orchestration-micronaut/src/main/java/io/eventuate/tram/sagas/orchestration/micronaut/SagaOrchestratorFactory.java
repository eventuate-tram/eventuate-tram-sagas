package io.eventuate.tram.sagas.orchestration.micronaut;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.sagas.orchestration.SagaCommandProducer;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepository;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepositoryJdbc;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class SagaOrchestratorFactory {
  @Singleton
  public SagaInstanceRepository sagaInstanceRepository(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                       IdGenerator idGenerator,
                                                       EventuateSchema eventuateSchema) {
    return new SagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor, idGenerator, eventuateSchema);
  }

  @Singleton
  public SagaCommandProducer sagaCommandProducer(CommandProducer commandProducer) {
    return new SagaCommandProducer(commandProducer);
  }
}
