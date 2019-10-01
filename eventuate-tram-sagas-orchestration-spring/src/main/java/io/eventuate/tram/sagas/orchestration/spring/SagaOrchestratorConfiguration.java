package io.eventuate.tram.sagas.orchestration.spring;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.spring.EventuateSchemaConfiguration;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.spring.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.sagas.orchestration.SagaCommandProducer;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepository;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepositoryJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Import({TramCommandProducerConfiguration.class, EventuateSchemaConfiguration.class})
public class SagaOrchestratorConfiguration {


  @Bean
  public SagaInstanceRepository sagaInstanceRepository(JdbcTemplate jdbcTemplate,
                                                       IdGenerator idGenerator,
                                                       EventuateSchema eventuateSchema) {
    return new SagaInstanceRepositoryJdbc(jdbcTemplate, idGenerator, eventuateSchema);
  }

  @Bean
  public SagaCommandProducer sagaCommandProducer(CommandProducer commandProducer) {
    return new SagaCommandProducer(commandProducer);
  }
}
