package io.eventuate.tram.sagas.inmemory;

import io.eventuate.common.inmemorydatabase.EventuateDatabaseScriptSupplier;
import io.eventuate.tram.inmemory.spring.TramInMemoryConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@Configuration
@Import({TramInMemoryConfiguration.class})
public class TramSagaInMemoryConfiguration {

  @Bean
  public EventuateDatabaseScriptSupplier eventuateCommonInMemoryScriptSupplierForEventuateTramSagas() {
    return () -> Collections.singletonList("eventuate-tram-sagas-embedded.sql");
  }

}
