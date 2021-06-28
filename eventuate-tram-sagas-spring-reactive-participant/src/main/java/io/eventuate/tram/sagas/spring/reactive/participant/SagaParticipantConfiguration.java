package io.eventuate.tram.sagas.spring.reactive.participant;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import io.eventuate.tram.sagas.reactive.participant.ReactiveSagaCommandDispatcherFactory;
import io.eventuate.tram.sagas.spring.reactive.common.EventuateReactiveTramSagaCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateReactiveTramSagaCommonConfiguration.class)
public class SagaParticipantConfiguration {
  @Bean
  public ReactiveSagaCommandDispatcherFactory sagaCommandDispatcherFactory(ReactiveMessageConsumer messageConsumer,
                                                                           ReactiveMessageProducer messageProducer,
                                                                           ReactiveSagaLockManager sagaLockManager) {
    return new ReactiveSagaCommandDispatcherFactory(messageConsumer, messageProducer, sagaLockManager);
  }
}
