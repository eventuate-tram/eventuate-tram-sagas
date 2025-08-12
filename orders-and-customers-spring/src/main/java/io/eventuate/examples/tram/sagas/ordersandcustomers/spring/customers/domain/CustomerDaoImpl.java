package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.customers.domain;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerDaoImpl implements CustomerDao {
  @Autowired
  private CustomerRepository customerRepository;

  @Override
  public Customer findById(long id) {
    return customerRepository
            .findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException("Customer with id=%s is not found".formatted(id)));
  }

  @Override
  public Customer save(Customer customer) {
    return customerRepository.save(customer);
  }
}
