package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandReplyProducer;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;

public class ReactiveSagaCommandDispatcherFactory {

  private final ReactiveMessageConsumer messageConsumer;
  private final ReactiveSagaLockManager sagaLockManager;
  private final ReactiveCommandReplyProducer commandReplyProducer;

  public ReactiveSagaCommandDispatcherFactory(ReactiveMessageConsumer messageConsumer, ReactiveSagaLockManager sagaLockManager, ReactiveCommandReplyProducer commandReplyProducer) {
    this.messageConsumer = messageConsumer;
    this.sagaLockManager = sagaLockManager;
    this.commandReplyProducer = commandReplyProducer;
  }

  public ReactiveSagaCommandDispatcher make(String commandDispatcherId, ReactiveCommandHandlers target) {
    return new ReactiveSagaCommandDispatcher(commandDispatcherId, target, messageConsumer, sagaLockManager, commandReplyProducer);
  }
}
