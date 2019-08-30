package io.eventuate.examples.tram.sagas.ordersandcustomers.micronaut.orders.domain;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Singleton
public class OrderDaoImpl implements OrderDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Order findById(long id) {
    return entityManager.find(Order.class, id);
  }

  @Override
  public Order save(Order order) {
    entityManager.persist(order);
    return order;
  }
}
