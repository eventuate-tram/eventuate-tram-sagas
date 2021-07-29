package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducer;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;

public class ReactiveSagaManagerFactory {

  private final ReactiveSagaInstanceRepository sagaInstanceRepository;
  private final ReactiveCommandProducer commandProducer;
  private final ReactiveMessageConsumer messageConsumer;
  private final ReactiveSagaLockManager sagaLockManager;
  private final ReactiveSagaCommandProducer sagaCommandProducer;

  public ReactiveSagaManagerFactory(ReactiveSagaInstanceRepository sagaInstanceRepository,
                                    ReactiveCommandProducer commandProducer,
                                    ReactiveMessageConsumer messageConsumer,
                                    ReactiveSagaLockManager sagaLockManager,
                                    ReactiveSagaCommandProducer sagaCommandProducer) {

    this.sagaInstanceRepository = sagaInstanceRepository;
    this.commandProducer = commandProducer;
    this.messageConsumer = messageConsumer;
    this.sagaLockManager = sagaLockManager;
    this.sagaCommandProducer = sagaCommandProducer;
  }

  public <SagaData> ReactiveSagaManagerImpl<SagaData> make(ReactiveSaga<SagaData> saga) {
    return new ReactiveSagaManagerImpl<>(saga,
            sagaInstanceRepository,
            commandProducer,
            messageConsumer,
            sagaLockManager,
            sagaCommandProducer);
  }


}
