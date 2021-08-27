package io.eventuate.tram.sagas.spring.reactive.orchestration;

import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducer;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSaga;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaCommandProducer;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceFactory;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceRepository;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceRepositoryJdbc;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaManagerFactory;
import io.eventuate.tram.sagas.spring.reactive.common.EventuateReactiveTramSagaCommonConfiguration;

import io.eventuate.tram.spring.reactive.commands.producer.ReactiveTramCommandProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collection;

@Configuration
@Import({ReactiveTramCommandProducerConfiguration.class, EventuateReactiveTramSagaCommonConfiguration.class})
public class ReactiveSagaOrchestratorConfiguration {


  @Bean
  public ReactiveSagaInstanceRepository sagaInstanceRepository(EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                               EventuateSchema eventuateSchema) {
    return new ReactiveSagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor,
            new ApplicationIdGenerator(),
            eventuateSchema);
  }

  @Bean
  public ReactiveSagaCommandProducer sagaCommandProducer(ReactiveCommandProducer commandProducer) {
    return new ReactiveSagaCommandProducer(commandProducer);
  }

  @Bean
  public ReactiveSagaInstanceFactory sagaInstanceFactory(ReactiveSagaInstanceRepository sagaInstanceRepository,
                                                         ReactiveCommandProducer commandProducer,
                                                         ReactiveMessageConsumer messageConsumer,
                                                         ReactiveSagaLockManager sagaLockManager,
                                                         ReactiveSagaCommandProducer sagaCommandProducer,
                                                         Collection<ReactiveSaga<?>> sagas) {

    ReactiveSagaManagerFactory smf = new ReactiveSagaManagerFactory(sagaInstanceRepository,
            commandProducer, messageConsumer, sagaLockManager, sagaCommandProducer);

    return new ReactiveSagaInstanceFactory(smf, sagas);
  }
}
