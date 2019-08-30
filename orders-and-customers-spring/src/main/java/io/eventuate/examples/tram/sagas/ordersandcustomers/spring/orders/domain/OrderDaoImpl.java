package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders.domain;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderDaoImpl implements OrderDao {

  @Autowired
  private OrderRepository orderRepository;

  @Override
  public Order findById(long id) {
    return orderRepository
            .findById(id)
            .orElseThrow(() ->
                    new IllegalArgumentException(String.format("Order with id=%s is not found", id)));
  }

  @Override
  public Order save(Order order) {
    return orderRepository.save(order);
  }
}
