package io.eventuate.tram.sagas.micronaut.common;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.id.IdGeneratorImpl;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class SagaLockManagerIntegrationTestFactory {

  @Singleton
  public IdGenerator idGenerator() {
    return new IdGeneratorImpl();
  }
}
