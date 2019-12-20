package io.eventuate.tram.sagas.participant.spring;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.common.spring.EventuateTramSagaCommonConfiguration;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateTramSagaCommonConfiguration.class)
public class SagaParticipantConfiguration {
  @Bean
  public SagaCommandDispatcherFactory sagaCommandDispatcherFactory(MessageConsumer messageConsumer,
                                                                   MessageProducer messageProducer,
                                                                   SagaLockManager sagaLockManager) {
    return new SagaCommandDispatcherFactory(messageConsumer, messageProducer, sagaLockManager);
  }
}
