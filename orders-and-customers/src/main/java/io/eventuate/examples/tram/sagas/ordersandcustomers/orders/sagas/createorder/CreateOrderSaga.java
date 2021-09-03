package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ApproveOrderCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.RejectOrderCommand;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

import java.util.Collections;

import static io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder.send;

public class CreateOrderSaga implements SimpleSaga<CreateOrderSagaData> {

  private DomainEventPublisher domainEventPublisher;

  public CreateOrderSaga(DomainEventPublisher domainEventPublisher) {
    this.domainEventPublisher = domainEventPublisher;
  }

  private SagaDefinition<SagaActions<CreateOrderSagaData>, CreateOrderSagaData> sagaDefinition =
          step()
                  .withCompensation(this::reject)
                  .step()
                  .invokeParticipant(this::reserveCredit)
                  .step()
                  .invokeParticipant(this::approve)
                  .build();


  @Override
  public SagaDefinition<SagaActions<CreateOrderSagaData>, CreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }


  private CommandWithDestination reserveCredit(CreateOrderSagaData data) {

    long orderId = data.getOrderId();
    Long customerId = data.getOrderDetails().getCustomerId();
    Money orderTotal = data.getOrderDetails().getOrderTotal();
    return send(new ReserveCreditCommand(customerId, orderId, orderTotal))
            .to("customerService")
            .build();
  }

  public CommandWithDestination reject(CreateOrderSagaData data) {
    return send(new RejectOrderCommand(data.getOrderId()))
            .to("orderService")
            .build();
  }

  private CommandWithDestination approve(CreateOrderSagaData data) {
    return send(new ApproveOrderCommand(data.getOrderId()))
            .to("orderService")
            .build();
  }

  @Override
  public void onSagaCompletedSuccessfully(String sagaId, CreateOrderSagaData createOrderSagaData) {
    domainEventPublisher.publish(CreateOrderSaga.class, sagaId, Collections.singletonList(new CreateOrderSagaCompletedSuccesfully(createOrderSagaData.getOrderId())));
  }

  @Override
  public void onSagaRolledBack(String sagaId, CreateOrderSagaData createOrderSagaData) {
    domainEventPublisher.publish(CreateOrderSaga.class, sagaId, Collections.singletonList(new CreateOrderSagaRolledBack(createOrderSagaData.getOrderId())));
  }
}
