package io.eventuate.tram.sagas.inmemory.micronaut;

import io.micronaut.context.annotation.Factory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.inject.Singleton;
import javax.sql.DataSource;


@Factory
public class TramSagaInMemoryFactory {

  @Singleton
  public DataSource dataSource() {
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    return builder
            .setType(EmbeddedDatabaseType.H2)
            .addScript("eventuate-tram-embedded-schema.sql")
            .addScript("eventuate-tram-sagas-embedded.sql")
            .build();
  }

}
