package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.customers;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerCommandHandler;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerService;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.eventuate.tram.spring.optimisticlocking.OptimisticLockingDecoratorConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@EntityScan("io.eventuate.examples.tram.sagas.ordersandcustomers")
@ComponentScan
@Import(OptimisticLockingDecoratorConfiguration.class)
public class CustomerConfiguration {

  @Bean
  public CustomerService customerService(CustomerDao customerDao) {
    return new CustomerService(customerDao);
  }

  @Bean
  public CustomerCommandHandler customerCommandHandler(CustomerDao customerDao) {
    return new CustomerCommandHandler(customerDao);
  }

  // TODO Exception handler for CustomerCreditLimitExceededException

  @Bean
  public CommandDispatcher consumerCommandDispatcher(CustomerCommandHandler target,
                                                     SagaCommandDispatcherFactory sagaCommandDispatcherFactory) {

    return sagaCommandDispatcherFactory.make("customerCommandDispatcher", target.commandHandlerDefinitions());
  }

}
