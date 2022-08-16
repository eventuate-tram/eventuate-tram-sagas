package io.eventuate.tram.sagas.spring.participant;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.eventuate.tram.sagas.spring.common.EventuateTramSagaCommonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(EventuateTramSagaCommonConfiguration.class)
public class SagaParticipantConfiguration {
  @Bean
  public SagaCommandDispatcherFactory sagaCommandDispatcherFactory(MessageConsumer messageConsumer,
                                                                   SagaLockManager sagaLockManager,
                                                                   CommandNameMapping commandNameMapping,
                                                                   CommandReplyProducer commandReplyProducer) {
    return new SagaCommandDispatcherFactory(messageConsumer, sagaLockManager, commandNameMapping, commandReplyProducer);
  }


}
