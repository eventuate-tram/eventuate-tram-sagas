package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.common.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CreditReservation;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CreditReservationRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CustomerCreditLimitExceededException;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CustomerNotFoundException;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public class CustomerService {
  private Logger logger = LoggerFactory.getLogger(getClass());

  private TransactionalOperator transactionalOperator;
  private CustomerRepository customerRepository;
  private CreditReservationRepository creditReservationRepository;

  public CustomerService(TransactionalOperator transactionalOperator,
                         CustomerRepository customerRepository,
                         CreditReservationRepository creditReservationRepository) {
    this.transactionalOperator = transactionalOperator;
    this.customerRepository = customerRepository;
    this.creditReservationRepository = creditReservationRepository;
  }

  public Mono<Customer> createCustomer(String name, Money creditLimit) {
    Customer customer  = new Customer(name, creditLimit.getAmount());
    return customerRepository.save(customer).as(transactionalOperator::transactional);
  }

  public Mono<?> reserveCredit(long orderId, long customerId, Money orderTotal) {

    Mono<Customer> possibleCustomer = customerRepository.findById(Mono.just(customerId));

    return possibleCustomer
            .flatMap(customer -> {
              return creditReservationRepository
                      .findAllByCustomerId(customerId)
                      .collectList()
                      .flatMap(creditReservations -> handleCreditReservation(creditReservations, customer, orderId, customerId, orderTotal));
            })
            .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerId)))
            .doOnError(throwable -> logger.error("credit reservation failed", throwable));
  }

  private Mono<?> handleCreditReservation(List<CreditReservation> creditReservations,
                                          Customer customer,
                                          long orderId,
                                          long customerId,
                                          Money orderTotal) {
    BigDecimal currentReservations =
            creditReservations.stream().map(CreditReservation::getReservation).reduce(BigDecimal.ZERO, BigDecimal::add);

    if (currentReservations.add(orderTotal.getAmount()).compareTo(customer.getCreditLimit()) <= 0) {
      logger.info("reserving credit (orderId: {}, customerId: {})", orderId, customerId);

      return creditReservationRepository.save(new CreditReservation(customerId, orderId, orderTotal.getAmount()));
    } else {
      logger.info("handling credit reservation failure (orderId: {}, customerId: {})", orderId, customerId);

      return Mono.error(new CustomerCreditLimitExceededException());
    }
  }
}
