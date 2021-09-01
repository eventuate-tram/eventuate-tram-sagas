package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderRepository  extends ReactiveCrudRepository<Order, Long> {
  Flux<Order> findAllByCustomerId(Long customerId);
}