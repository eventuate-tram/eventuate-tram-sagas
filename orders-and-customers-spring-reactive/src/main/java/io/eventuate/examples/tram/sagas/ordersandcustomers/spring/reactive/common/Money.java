package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class Money {

  public static final Money ZERO = new Money(0);
  private BigDecimal amount;

  public Money() {
  }

  public Money(int i) {
    this.amount = new BigDecimal(i);
  }
  public Money(String s) {
    this.amount = new BigDecimal(s);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public Money(BigDecimal amount) {
    this.amount = amount;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public boolean isGreaterThanOrEqual(Money other) {
    return amount.compareTo(other.amount) >= 0;
  }

  public Money add(Money other) {
    return new Money(amount.add(other.amount));
  }
  public Money subtract(Money other) {
    return new Money(amount.subtract(other.amount));
  }
}