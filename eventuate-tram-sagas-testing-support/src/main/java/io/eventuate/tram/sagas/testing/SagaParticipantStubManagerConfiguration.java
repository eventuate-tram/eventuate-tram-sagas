package io.eventuate.tram.sagas.testing;

import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.events.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramCommandProducerConfiguration.class, TramEventsPublisherConfiguration.class,})
public class SagaParticipantStubManagerConfiguration {

  @Bean
  public SagaParticipantStubManager sagaParticipantStubManager(SagaParticipantChannels sagaParticipantChannels, MessageConsumer messageConsumer, MessageProducer messageProducer) {
    return new SagaParticipantStubManager(sagaParticipantChannels, messageConsumer, messageProducer);
  }

}
