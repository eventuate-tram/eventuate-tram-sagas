package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomerService {

  @Autowired
  private CustomerRepository customerRepository;

  public Customer createCustomer(String name, Money creditLimit) {
    Customer customer  = new Customer(name, creditLimit);
    return customerRepository.save(customer);
  }
}
