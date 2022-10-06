package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.commands.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CustomerCreditLimitExceededException;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CustomerNotFoundException;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.replies.CustomerCreditLimitExceeded;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.replies.CustomerCreditReserved;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.replies.CustomerNotFound;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.sagas.reactive.participant.ReactiveSagaCommandHandlersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlerReplyBuilder.withSuccess;

public class CustomerCommandHandler {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private CustomerService customerService;

  public CustomerCommandHandler(CustomerService customerService) {
    this.customerService = customerService;
  }

  public ReactiveCommandHandlers commandHandlerDefinitions() {
    return ReactiveSagaCommandHandlersBuilder
            .fromChannel("customerService")
            .onMessage(ReserveCreditCommand.class, this::reserveCredit)
            .build();
  }

  public Mono<Message> reserveCredit(CommandMessage<ReserveCreditCommand> cm) {
    ReserveCreditCommand cmd = cm.getCommand();

    logger.info("reserveCredit OrderId={}, CustomerID={}", cmd.getOrderId(), cmd.getCustomerId());

    return customerService
            .reserveCredit(cmd.getOrderId(), cmd.getCustomerId(), cmd.getOrderTotal())
            .flatMap(m -> {
              logger.info("reservedCredit Success OrderId={}, CustomerID={}", cmd.getOrderId(), cmd.getCustomerId());
              return withSuccess(new CustomerCreditReserved());
            })
            .onErrorResume(CustomerNotFoundException.class, e -> {
              logger.info("reservedCredit CustomerNotFoundException OrderId={}, CustomerID={}", cmd.getOrderId(), cmd.getCustomerId());
              return withFailure(new CustomerNotFound());
            })
            .onErrorResume(CustomerCreditLimitExceededException.class, e -> {
              logger.info("reservedCredit CustomerCreditLimitExceededException OrderId={}, CustomerID={}", cmd.getOrderId(), cmd.getCustomerId());
              return withFailure(new CustomerCreditLimitExceeded());
            });
  }

}
