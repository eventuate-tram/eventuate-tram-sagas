package io.eventuate.tram.sagas.simpledsl.notifications;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;

public class ConditionalNotificationBasedCreateOrderSagaData {

  private boolean fulfillOrder = true;
  private boolean releaseInventory = true;

  public CommandWithDestination reserveCredit() {
    return new CommandWithDestination("customerService", null, new ReserveCreditCommand());
  }

  public CommandWithDestination releaseCredit() {
    return new CommandWithDestination("customerService", null, new ReleaseCreditCommand());
  }

  public CommandWithDestination reserveInventory() {
    return new CommandWithDestination("inventoryService", null, new ReserveInventory());
  }

  public CommandWithDestination releaseInventory() {
    return new CommandWithDestination("inventoryService", null, new ReleaseInventory());
  }

  public CommandWithDestination fulfillOrder() {
    return new CommandWithDestination("fulfillmentService", null, new FulfillOrder());
  }

  public ConditionalNotificationBasedCreateOrderSagaData skipFulfillOrder() {
    this.fulfillOrder = false;
    return this;
  }

  public ConditionalNotificationBasedCreateOrderSagaData skipReleaseInventory() {
    this.releaseInventory = false;
    return this;
  }

  public boolean isFulfillOrder() {
    return fulfillOrder;
  }

  public boolean isReleaseInventory() {
    return releaseInventory;
  }
}
