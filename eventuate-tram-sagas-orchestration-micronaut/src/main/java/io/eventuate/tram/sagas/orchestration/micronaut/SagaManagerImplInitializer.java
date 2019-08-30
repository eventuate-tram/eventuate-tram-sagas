package io.eventuate.tram.sagas.orchestration.micronaut;

import io.eventuate.tram.sagas.orchestration.SagaManager;
import io.eventuate.tram.sagas.orchestration.SagaManagerImpl;
import io.micronaut.context.annotation.Context;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;

@Context
public class SagaManagerImplInitializer {

  @Inject
  private SagaManager[] sagaManagers;

  @PostConstruct
  public void init() {
    Arrays.stream(sagaManagers).forEach(SagaManager::subscribeToReplyChannel);
  }
}
