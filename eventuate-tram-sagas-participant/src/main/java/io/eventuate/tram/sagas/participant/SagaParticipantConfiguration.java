package io.eventuate.tram.sagas.participant;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.common.SagaLockManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaParticipantConfiguration {

  @Bean
  public SagaLockManager sagaLockManager() {
    return new SagaLockManagerImpl();
  }

  @Bean
  public SagaCommandDispatcherFactory sagaCommandDispatcherFactory(MessageConsumer messageConsumer, MessageProducer messageProducer, SagaLockManager sagaLockManager) {
    return new SagaCommandDispatcherFactory(messageConsumer, messageProducer, sagaLockManager);
  }
}
