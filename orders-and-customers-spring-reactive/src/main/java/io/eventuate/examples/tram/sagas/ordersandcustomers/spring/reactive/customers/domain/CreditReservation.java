package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("credit_reservation")
public class CreditReservation {

  @Id
  private Long id;

  private Long customerId;

  private long orderId;

  private BigDecimal reservation;

  public CreditReservation() {
  }

  public CreditReservation(Long customerId, long orderId, BigDecimal reservation) {
    this.customerId = customerId;
    this.orderId = orderId;
    this.reservation = reservation;
  }

  public BigDecimal getReservation() {
    return reservation;
  }
}
