package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants;

import io.eventuate.tram.commands.common.Command;

public class ApproveOrderCommand implements Command {
  private long orderId;

  private ApproveOrderCommand() {
  }


  public ApproveOrderCommand(long orderId) {

    this.orderId = orderId;
  }

  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }
}
