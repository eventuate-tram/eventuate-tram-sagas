package io.eventuate.examples.tram.sagas.ordersandcustomers.micronaut.customers.domain;

import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer;
import io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.CustomerDao;
import io.micronaut.transaction.annotation.TransactionalAdvice;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class CustomerDaoImpl implements CustomerDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Customer findById(long id) {
    return entityManager.find(Customer.class, id);
  }

  @Override
  @TransactionalAdvice
  public Customer save(Customer customer) {
    entityManager.persist(customer);
    return customer;
  }
}
