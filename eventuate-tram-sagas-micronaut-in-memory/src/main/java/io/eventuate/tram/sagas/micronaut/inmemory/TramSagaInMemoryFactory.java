package io.eventuate.tram.sagas.micronaut.inmemory;

import io.eventuate.common.inmemorydatabase.EventuateDatabaseScriptSupplier;
import io.micronaut.context.annotation.Factory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collections;


@Factory
public class TramSagaInMemoryFactory {
  @Singleton
  @Named("TramSagasEventuateDatabaseScriptSupplier")
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForEventuateTramSagas() {
    return () -> Collections.singletonList("eventuate-tram-sagas-embedded.sql");
  }
}
