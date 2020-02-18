package io.eventuate.tram.sagas.orchestration.micronaut;

import io.eventuate.tram.sagas.orchestration.SagaManager;
import io.micronaut.context.annotation.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;

@Context
public class SagaManagerImplInitializer {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Inject
  private SagaManager[] sagaManagers;

  @PostConstruct
  public void init() {
    logger.info("Initializing saga managers");
    Arrays.stream(sagaManagers).forEach(SagaManager::subscribeToReplyChannel);
    logger.info("Initialized saga managers");
  }
}
