package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.proxy;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ReserveCreditCommand;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import io.eventuate.tram.sagas.simpledsl.CommandEndpointBuilder;


/*

public class CreateOrderSaga implements SimpleSaga<CreateOrderSagaData> {

  CustomerServiceProxy customerService;

  private SagaDefinition<CreateOrderSagaData> sagaDefinition =
          step()
            .withCompensation(this::reject)
          .step()
            .invokeParticipant(customerService, this::makeReserveCreditCommand)
            .onReply(CustomerServiceProxy.success, handler)       <<< Build time checking????
          .step()
            .invokeParticipant(this::approve)
          .build();
 */

public class CustomerServiceProxy {

  public final CommandEndpoint<ReserveCreditCommand> reserveCredit = CommandEndpointBuilder
            .forCommand(ReserveCreditCommand.class)
            .withChannel("customerService")
            .withReply(Success.class)
            .build();

}
