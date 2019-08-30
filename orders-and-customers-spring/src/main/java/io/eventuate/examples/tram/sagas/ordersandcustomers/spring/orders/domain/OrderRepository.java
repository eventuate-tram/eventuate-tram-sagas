package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders.domain;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {
}
