package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service;

import io.eventuate.tram.commands.common.Command;

public class RejectOrderCommand implements Command {

  private long orderId;

  private RejectOrderCommand() {
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public RejectOrderCommand(long orderId) {

    this.orderId = orderId;
  }

  public long getOrderId() {
    return orderId;
  }
}
