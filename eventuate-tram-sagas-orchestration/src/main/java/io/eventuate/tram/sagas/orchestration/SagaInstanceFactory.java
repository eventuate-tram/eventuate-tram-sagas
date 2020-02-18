package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class SagaInstanceFactory {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private ConcurrentMap<Saga<?>, InitializedSagaManager> map = new ConcurrentHashMap<>();
  private SagaManagerFactory sagaManagerFactory;

  public SagaInstanceFactory(SagaManagerFactory sagaManagerFactory) {
    this.sagaManagerFactory = sagaManagerFactory;
  }

  public <SagaData> SagaInstance create(Saga<SagaData> saga, SagaData data) {
    InitializedSagaManager initializedSagaManager = map.computeIfAbsent(saga, this::makeSagaManager);
    initializedSagaManager.checkInitialization();
    SagaManager<SagaData> sm = initializedSagaManager.getSagaManager();
    return sm.create(data);
  }

  private <SagaData> InitializedSagaManager makeSagaManager(Saga<SagaData> saga) {
    SagaManagerImpl<SagaData> sagaDataSagaManager = sagaManagerFactory.make(saga);
    CompletableFuture<Void> cf = CompletableFuture.runAsync(sagaDataSagaManager::subscribeToReplyChannel);
    return new InitializedSagaManager(sagaDataSagaManager, cf);
  }


  private class InitializedSagaManager {
    private final SagaManager<?> sagaManager;
    private final CompletableFuture<Void> initializationFuture;

    public <SagaData> InitializedSagaManager(SagaManager<SagaData> sagaManager, CompletableFuture<Void> initializationFuture) {
      this.sagaManager = sagaManager;
      this.initializationFuture = initializationFuture;
    }

    public void checkInitialization() {
      try {
        initializationFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        logger.error("Saga initialization failed", e);
        throw new RuntimeException(e);
      }
    }

    public <SagaData> SagaManager<SagaData> getSagaManager() {
      return (SagaManager<SagaData>) sagaManager;
    }
  }
}
