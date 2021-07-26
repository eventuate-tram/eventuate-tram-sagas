package io.eventuate.tram.sagas.spring.reactive.orchestration;

import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceRepository;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceRepositoryJdbc;
import io.eventuate.tram.sagas.spring.reactive.common.EventuateReactiveTramSagaCommonConfiguration;

import io.eventuate.tram.spring.reactive.commands.producer.ReactiveTramCommandProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ReactiveTramCommandProducerConfiguration.class, EventuateReactiveTramSagaCommonConfiguration.class})
public class SagaOrchestratorConfiguration {


  @Bean
  public ReactiveSagaInstanceRepository sagaInstanceRepository(EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                               EventuateSchema eventuateSchema) {
    return new ReactiveSagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor,
            new ApplicationIdGenerator(),
            eventuateSchema);
  }
//
//  @Bean
//  public SagaCommandProducer sagaCommandProducer(CommandProducer commandProducer) {
//    return new SagaCommandProducer(commandProducer);
//  }
//
//  @Bean
//  public SagaInstanceFactory sagaInstanceFactory(SagaInstanceRepository sagaInstanceRepository, CommandProducer
//          commandProducer, MessageConsumer messageConsumer,
//                                                 SagaLockManager sagaLockManager, SagaCommandProducer sagaCommandProducer, Collection<Saga<?>> sagas) {
//    SagaManagerFactory smf = new SagaManagerFactory(sagaInstanceRepository, commandProducer, messageConsumer,
//            sagaLockManager, sagaCommandProducer);
//    return new SagaInstanceFactory(smf, sagas);
//  }
}
