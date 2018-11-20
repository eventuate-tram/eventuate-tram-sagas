package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import io.eventuate.tram.events.common.DomainEvent;

public class CreateOrderSagaRolledBack implements DomainEvent {

  private long orderId;

  public CreateOrderSagaRolledBack() {
  }

  public CreateOrderSagaRolledBack(long orderId) {
    this.orderId = orderId;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }


}
