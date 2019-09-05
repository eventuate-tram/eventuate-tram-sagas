package io.eventuate.tram.sagas.inmemory.micronaut;

import io.eventuate.tram.inmemory.micronaut.EmbeddedSchema;
import io.micronaut.context.annotation.Factory;

import javax.inject.Named;
import javax.inject.Singleton;


@Factory
public class TramSagaInMemoryFactory {

  @Named("sagasEmbeddedSchema")
  @Singleton
  public EmbeddedSchema sagaEmbeddedSchema() {
    return new EmbeddedSchema("eventuate-tram-sagas-embedded.sql");
  }
}
