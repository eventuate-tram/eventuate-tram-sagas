package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.commands.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.participant.SagaLockManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collection;

@Configuration
@Import({TramCommandProducerConfiguration.class})
public class SagaOrchestratorConfiguration {

  @Bean
  public SagaOrchestrator sagaOrchestrator(final SagaRegistrar sagaRegistrar,
                                           final ChannelMapping channelMapping,
                                           final MessageConsumer messageConsumer,
                                           final SagaLockManager sagaLockManager,
                                           final CommandProducer commandProducer,
                                           final SagaCommandProducer sagaCommandProducer,
                                           final SagaInstanceRepository sagaInstanceRepository) {
    return new SagaOrchestrator(sagaRegistrar, channelMapping, messageConsumer,
            sagaLockManager, commandProducer, sagaCommandProducer, sagaInstanceRepository);
  }

  @Bean
  public SagaRegistrar sagaRegistrar(final Collection<Saga> sagas) {
    return new SimpleSagaRegistrar(sagas);
  }

  @Bean
  public SagaInstanceRepository sagaInstanceRepository() {
    return new SagaInstanceRepositoryJdbc();
  }

  @Bean
  public SagaCommandProducer sagaCommandProducer(CommandProducer commandProducer) {
    return new SagaCommandProducer(commandProducer);
  }
}
