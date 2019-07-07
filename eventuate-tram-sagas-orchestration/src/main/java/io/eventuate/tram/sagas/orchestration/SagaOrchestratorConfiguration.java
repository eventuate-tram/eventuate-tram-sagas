package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.jdbc.EventuateSchemaConfiguration;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramCommandProducerConfiguration.class, EventuateSchemaConfiguration.class})
public class SagaOrchestratorConfiguration {


  @Bean
  public SagaInstanceRepository sagaInstanceRepository(EventuateSchema eventuateSchema) {
    return new SagaInstanceRepositoryJdbc(eventuateSchema);
  }

  @Bean
  public SagaCommandProducer sagaCommandProducer(CommandProducer commandProducer) {
    return new SagaCommandProducer(commandProducer);
  }
}
