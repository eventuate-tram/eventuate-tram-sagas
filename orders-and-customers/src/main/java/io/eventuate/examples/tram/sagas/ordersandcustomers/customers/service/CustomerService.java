package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerDao;

public class CustomerService {

  private CustomerDao customerDao;

  public CustomerService(CustomerDao customerDao) {
    this.customerDao = customerDao;
  }

  public Customer createCustomer(String name, Money creditLimit) {
    Customer customer  = new Customer(name, creditLimit);
    return customerDao.save(customer);
  }
}
