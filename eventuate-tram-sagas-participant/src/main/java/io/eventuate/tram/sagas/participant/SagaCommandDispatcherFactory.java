package io.eventuate.tram.sagas.participant;

import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.sagas.common.SagaLockManager;

public class SagaCommandDispatcherFactory {

  private final MessageConsumer messageConsumer;
  private final MessageProducer messageProducer;
  private final SagaLockManager sagaLockManager;

  public SagaCommandDispatcherFactory(MessageConsumer messageConsumer, MessageProducer messageProducer, SagaLockManager sagaLockManager) {
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
    this.sagaLockManager = sagaLockManager;
  }

  public SagaCommandDispatcher make(String commandDispatcherId, CommandHandlers target) {
    return new SagaCommandDispatcher(commandDispatcherId, target, messageConsumer, messageProducer, sagaLockManager);
  }
}
