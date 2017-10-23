package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain;


import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderDetails;
import io.eventuate.tram.events.ResultWithEvents;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Collections;

@Entity
@Table(name="orders")
@Access(AccessType.FIELD)
public class Order {

  @Id
  @GeneratedValue
  private Long id;

  private OrderState state;

  @Embedded
  private OrderDetails orderDetails;

  public Order() {
  }

  public Order(OrderDetails orderDetails) {
    this.orderDetails = orderDetails;
    this.state = OrderState.PENDING;
  }

  public static ResultWithEvents<Order> createOrder(OrderDetails orderDetails) {
    return new ResultWithEvents<Order>(new Order(orderDetails), Collections.emptyList());
  }

  public Long getId() {
    return id;
  }

  public void noteCreditReserved() {
    this.state = OrderState.APPROVED;
  }

  public void noteCreditReservationFailed() {
    this.state = OrderState.REJECTED;
  }

  public OrderState getState() {
    return state;
  }
}
