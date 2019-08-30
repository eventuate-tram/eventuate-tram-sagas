package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.customers.domain;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
}
