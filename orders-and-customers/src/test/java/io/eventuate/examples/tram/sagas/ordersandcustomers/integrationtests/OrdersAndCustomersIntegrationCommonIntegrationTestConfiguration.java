package io.eventuate.examples.tram.sagas.ordersandcustomers.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.CustomerConfiguration;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.OrderConfiguration;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder.CreateOrderSaga;
import io.eventuate.tram.commands.spring.producer.TramCommandProducerConfiguration;
import io.eventuate.tram.events.spring.publisher.TramEventsPublisherConfiguration;
import io.eventuate.tram.events.spring.subscriber.TramEventSubscriberConfiguration;
import io.eventuate.tram.events.subscriber.DomainEventDispatcher;
import io.eventuate.tram.events.subscriber.DomainEventDispatcherFactory;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.orchestration.SagaOrchestratorConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EnableTransactionManagement
@Import({
        OrderConfiguration.class,
        CustomerConfiguration.class,
        TramEventsPublisherConfiguration.class,
        TramEventSubscriberConfiguration.class,
        TramCommandProducerConfiguration.class,
        SagaOrchestratorConfiguration.class

})
public class OrdersAndCustomersIntegrationCommonIntegrationTestConfiguration {

  @Bean
  public ChannelMapping channelMapping(TramCommandsAndEventsIntegrationData data) {
    return DefaultChannelMapping.builder()
            .with("CustomerAggregate", data.getAggregateDestination())
            .with("customerService", data.getCommandChannel())
            .with(CreateOrderSaga.class.getName(), data.getSagaEventsChannel())
            .build();
  }


  @Bean
  public TramCommandsAndEventsIntegrationData tramCommandsAndEventsIntegrationData() {
    return new TramCommandsAndEventsIntegrationData();
  }

  @Bean
  public SagaEventsConsumer sagaEventsConsumer() {
    return new SagaEventsConsumer();
  }

  @Bean
  public DomainEventDispatcher domainEventDispatcher(TramCommandsAndEventsIntegrationData tramCommandsAndEventsIntegrationData, SagaEventsConsumer sagaEventsConsumer, DomainEventDispatcherFactory domainEventDispatcherFactory) {
    return domainEventDispatcherFactory.make(tramCommandsAndEventsIntegrationData.getEventDispatcherId(), sagaEventsConsumer.domainEventHandlers());
  }

}
