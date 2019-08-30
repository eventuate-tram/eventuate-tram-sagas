package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain;

public interface CustomerDao {
  Customer findById(long id);
  Customer save(Customer customer);
}
