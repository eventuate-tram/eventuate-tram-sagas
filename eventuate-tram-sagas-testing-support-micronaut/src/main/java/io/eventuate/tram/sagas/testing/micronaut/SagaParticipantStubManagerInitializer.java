package io.eventuate.tram.sagas.testing.micronaut;

import io.eventuate.tram.sagas.testing.SagaParticipantStubManager;
import io.micronaut.context.annotation.Context;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;

@Context
public class SagaParticipantStubManagerInitializer {

  @Inject
  private SagaParticipantStubManager[] sagaParticipantStubManagers;

  @PostConstruct
  public void init() {
    Arrays.stream(sagaParticipantStubManagers).forEach(SagaParticipantStubManager::initialize);
  }
}
