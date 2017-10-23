package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({TramCommandProducerConfiguration.class})
public class SagaOrchestratorConfiguration {

  @Bean
  public AggregateInstanceSubscriptionsDAO aggregateInstanceSubscriptionsDAO() {
    return new AggregateInstanceSubscriptionsDAO();
  }

  @Bean
  public SagaInstanceRepository sagaInstanceRepository() {
    return new SagaInstanceRepositoryJdbc();
  }

}
