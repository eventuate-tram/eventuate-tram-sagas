package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain;

public interface OrderDao {
  Order findById(long id);
  Order save(Order order);
}
