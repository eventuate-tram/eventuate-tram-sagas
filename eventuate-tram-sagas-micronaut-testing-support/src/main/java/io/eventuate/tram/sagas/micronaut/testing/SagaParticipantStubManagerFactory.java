package io.eventuate.tram.sagas.micronaut.testing;

import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.sagas.testing.SagaParticipantChannels;
import io.eventuate.tram.sagas.testing.SagaParticipantStubManager;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class SagaParticipantStubManagerFactory {

  @Singleton
  public SagaParticipantStubManager sagaParticipantStubManager(SagaParticipantChannels sagaParticipantChannels, MessageConsumer messageConsumer, MessageProducer messageProducer) {
    return new SagaParticipantStubManager(sagaParticipantChannels, messageConsumer, messageProducer);
  }
}
