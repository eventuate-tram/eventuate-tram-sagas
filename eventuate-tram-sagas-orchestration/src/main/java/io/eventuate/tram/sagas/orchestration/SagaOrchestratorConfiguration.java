package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.spring.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.jdbc.spring.CommonJdbcMessagingConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramCommandProducerConfiguration.class, CommonJdbcMessagingConfiguration.class})
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
