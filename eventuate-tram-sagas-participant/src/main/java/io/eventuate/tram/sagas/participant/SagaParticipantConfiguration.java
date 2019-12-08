package io.eventuate.tram.sagas.participant;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.sagas.common.EventuateTramSagaCommonConfiguration;
import io.eventuate.tram.sagas.common.SagaLockManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateTramSagaCommonConfiguration.class)
public class SagaParticipantConfiguration {

  @Bean
  public SagaCommandDispatcherFactory sagaCommandDispatcherFactory(MessageConsumer messageConsumer, MessageProducer messageProducer, SagaLockManager sagaLockManager) {
    return new SagaCommandDispatcherFactory(messageConsumer, messageProducer, sagaLockManager);
  }
}
