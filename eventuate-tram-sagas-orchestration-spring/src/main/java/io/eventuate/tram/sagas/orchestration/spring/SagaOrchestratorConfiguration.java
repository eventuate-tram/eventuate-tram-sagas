package io.eventuate.tram.sagas.orchestration.spring;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.spring.EventuateSchemaConfiguration;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.spring.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramCommandProducerConfiguration.class, EventuateSchemaConfiguration.class})
public class SagaOrchestratorConfiguration {


  @Bean
  public SagaInstanceRepository sagaInstanceRepository(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                       IdGenerator idGenerator,
                                                       EventuateSchema eventuateSchema) {
    return new SagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor, idGenerator, eventuateSchema);
  }

  @Bean
  public SagaCommandProducer sagaCommandProducer(CommandProducer commandProducer) {
    return new SagaCommandProducer(commandProducer);
  }

  @Bean
  public SagaInstanceFactory sagaInstanceFactory(SagaInstanceRepository sagaInstanceRepository, CommandProducer
          commandProducer, MessageConsumer messageConsumer,
                                                 SagaLockManager sagaLockManager, SagaCommandProducer sagaCommandProducer) {
    SagaManagerFactory smf = new SagaManagerFactory(sagaInstanceRepository, commandProducer, messageConsumer,
            sagaLockManager, sagaCommandProducer);
    return new SagaInstanceFactory(smf);
  }
}
