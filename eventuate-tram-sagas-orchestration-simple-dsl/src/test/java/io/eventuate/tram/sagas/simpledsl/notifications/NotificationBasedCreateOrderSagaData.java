package io.eventuate.tram.sagas.simpledsl.notifications;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;

public class NotificationBasedCreateOrderSagaData {

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
}
