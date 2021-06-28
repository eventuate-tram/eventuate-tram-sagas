package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;

public class ReactiveSagaCommandDispatcherFactory {

  private final ReactiveMessageConsumer messageConsumer;
  private final ReactiveMessageProducer messageProducer;
  private final ReactiveSagaLockManager sagaLockManager;

  public ReactiveSagaCommandDispatcherFactory(ReactiveMessageConsumer messageConsumer, ReactiveMessageProducer messageProducer, ReactiveSagaLockManager sagaLockManager) {
    this.messageConsumer = messageConsumer;
    this.messageProducer = messageProducer;
    this.sagaLockManager = sagaLockManager;
  }

  public ReactiveSagaCommandDispatcher make(String commandDispatcherId, ReactiveCommandHandlers target) {
    return new ReactiveSagaCommandDispatcher(commandDispatcherId, target, messageConsumer, messageProducer, sagaLockManager);
  }
}
