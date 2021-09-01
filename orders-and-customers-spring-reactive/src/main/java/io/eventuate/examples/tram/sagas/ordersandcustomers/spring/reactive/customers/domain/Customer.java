package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("customer")
public class Customer {

  @Id
  private Long id;
  private String name;

  private BigDecimal creditLimit;

  private Long creationTime;

  @Version
  private Long version;

  public Customer() {
  }

  public Customer(String name, BigDecimal creditLimit) {
    this.name = name;
    this.creditLimit = creditLimit;
    this.creationTime = System.currentTimeMillis();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public BigDecimal getCreditLimit() {
    return creditLimit;
  }
}
