package io.eventuate.tram.sagas.orchestration.micronaut;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.*;
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

  @Singleton
  public SagaInstanceFactory sagaInstanceFactory(SagaInstanceRepository sagaInstanceRepository,
                                                 CommandProducer commandProducer, MessageConsumer messageConsumer,
                                                 SagaLockManager sagaLockManager, SagaCommandProducer sagaCommandProducer) {
    SagaManagerFactory smf = new SagaManagerFactory(sagaInstanceRepository, commandProducer, messageConsumer,
            sagaLockManager, sagaCommandProducer);
    return new SagaInstanceFactory(smf);
  }
}
