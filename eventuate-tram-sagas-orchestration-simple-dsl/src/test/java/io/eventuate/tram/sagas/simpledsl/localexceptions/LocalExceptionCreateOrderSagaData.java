package io.eventuate.tram.sagas.simpledsl.localexceptions;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;
import io.eventuate.tram.sagas.simpledsl.notifications.FulfillOrder;
import io.eventuate.tram.sagas.simpledsl.notifications.ReleaseInventory;
import io.eventuate.tram.sagas.simpledsl.notifications.ReserveInventory;

public class LocalExceptionCreateOrderSagaData {


  private boolean invalidOrder = false;

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

  public void saveInvalidOrder(InvalidOrderException e) {
    this.invalidOrder = true;
  }

  public boolean isInvalidOrder() {
    return invalidOrder;
  }
}
