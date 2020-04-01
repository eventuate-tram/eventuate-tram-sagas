package io.eventuate.tram.sagas.orchestration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SagaInstanceFactory {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private ConcurrentMap<Saga<?>, SagaManager<?>> sagaManagers = new ConcurrentHashMap<>();
  private SagaManagerFactory sagaManagerFactory;

  public SagaInstanceFactory(SagaManagerFactory sagaManagerFactory) {
    this.sagaManagerFactory = sagaManagerFactory;
  }

  public <SagaData> SagaInstance create(Saga<SagaData> saga, SagaData data) {
    SagaManager<SagaData>  sagaManager = (SagaManager<SagaData>)sagaManagers.computeIfAbsent(saga, this::makeSagaManager);
    return sagaManager.create(data);
  }

  private <SagaData> SagaManager<SagaData> makeSagaManager(Saga<SagaData> saga) {
    SagaManagerImpl<SagaData> sagaDataSagaManager = sagaManagerFactory.make(saga);
    sagaDataSagaManager.subscribeToReplyChannel();
    return sagaDataSagaManager;
  }
}
