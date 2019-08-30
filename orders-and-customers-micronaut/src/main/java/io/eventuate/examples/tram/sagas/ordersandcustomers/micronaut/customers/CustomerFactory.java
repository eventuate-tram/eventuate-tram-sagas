package io.eventuate.examples.tram.sagas.ordersandcustomers.micronaut.customers;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerCommandHandler;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service.CustomerService;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class CustomerFactory {

  @Singleton
  public CustomerService customerService(CustomerDao customerDao) {
    return new CustomerService(customerDao);
  }

  @Singleton
  public CustomerCommandHandler customerCommandHandler(CustomerDao customerDao) {
    return new CustomerCommandHandler(customerDao);
  }

  // TODO Exception handler for CustomerCreditLimitExceededException

  @Singleton
  public CommandDispatcher consumerCommandDispatcher(CustomerCommandHandler target,
                                                     SagaLockManager sagaLockManager, SagaCommandDispatcherFactory sagaCommandDispatcherFactory) {

    return sagaCommandDispatcherFactory.make("customerCommandDispatcher", target.commandHandlerDefinitions());
  }

}
