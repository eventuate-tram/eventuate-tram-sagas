package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Collections;
import java.util.Map;

@Entity
@Table(name="Customer")
@Access(AccessType.FIELD)
public class Customer {

  @Id
  @GeneratedValue
  private Long id;
  private String name;

  @Embedded
  private Money creditLimit;

  @ElementCollection
  private Map<Long, Money> creditReservations;

  Money availableCredit() {
    return creditLimit.subtract(creditReservations.values().stream().reduce(Money.ZERO, Money::add));
  }

  public Customer() {
  }

  public Customer(String name, Money creditLimit) {
    this.name = name;
    this.creditLimit = creditLimit;
    this.creditReservations = Collections.emptyMap();
  }

  public Long getId() {
    return id;
  }

  public void reserveCredit(Long orderId, Money orderTotal) {
    if (availableCredit().isGreaterThanOrEqual(orderTotal)) {
      creditReservations.put(orderId, orderTotal);
    } else
      throw new CustomerCreditLimitExceededException();
  }
}
