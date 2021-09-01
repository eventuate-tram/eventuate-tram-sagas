package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CreditReservationRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CustomerRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.service.CustomerCommandHandler;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.service.CustomerService;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandDispatcher;
import io.eventuate.tram.sagas.reactive.participant.ReactiveSagaCommandDispatcherFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@EnableAutoConfiguration
public class CustomerConfiguration {

  @Bean
  public CustomerService customerService(TransactionalOperator transactionalOperator,
                                         CustomerRepository customerRepository,
                                         CreditReservationRepository creditReservationRepository) {
    return new CustomerService(transactionalOperator, customerRepository, creditReservationRepository);
  }

  @Bean
  public CustomerCommandHandler customerCommandHandler(CustomerService customerService) {
    return new CustomerCommandHandler(customerService);
  }

  @Bean
  public ReactiveCommandDispatcher consumerCommandDispatcher(CustomerCommandHandler target,
                                                             ReactiveSagaCommandDispatcherFactory sagaCommandDispatcherFactory) {

    return sagaCommandDispatcherFactory.make("customerCommandDispatcher", target.commandHandlerDefinitions());
  }

}
