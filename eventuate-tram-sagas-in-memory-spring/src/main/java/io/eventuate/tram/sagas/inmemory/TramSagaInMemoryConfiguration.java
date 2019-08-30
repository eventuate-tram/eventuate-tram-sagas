package io.eventuate.tram.sagas.inmemory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
public class TramSagaInMemoryConfiguration {

  @Bean
  public DataSource dataSource() {
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    return builder
            .setType(EmbeddedDatabaseType.H2)
            .addScript("eventuate-tram-embedded-schema.sql")
            .addScript("eventuate-tram-sagas-embedded.sql")
            .build();
  }

}
