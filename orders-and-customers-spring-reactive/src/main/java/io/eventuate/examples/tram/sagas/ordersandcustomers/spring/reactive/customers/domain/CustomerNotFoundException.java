package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain;

public class CustomerNotFoundException extends RuntimeException {
  public CustomerNotFoundException(long customerId) {
    super("Customer not found: " + customerId);
  }
}
