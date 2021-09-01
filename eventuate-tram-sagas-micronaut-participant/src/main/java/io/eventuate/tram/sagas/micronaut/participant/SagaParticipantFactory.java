package io.eventuate.tram.sagas.micronaut.participant;

import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class SagaParticipantFactory {
  @Singleton
  public SagaCommandDispatcherFactory sagaCommandDispatcherFactory(MessageConsumer messageConsumer,
                                                                   MessageProducer messageProducer,
                                                                   SagaLockManager sagaLockManager,
                                                                   CommandNameMapping commandNameMapping) {
    return new SagaCommandDispatcherFactory(messageConsumer, messageProducer, sagaLockManager, commandNameMapping);
  }
}
