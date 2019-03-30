package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderRepository;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ApproveOrderCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.RejectOrderCommand;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.events.ResultWithEvents;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder.send;

public class LocalCreateOrderSaga implements SimpleSaga<LocalCreateOrderSagaData> {

  private DomainEventPublisher domainEventPublisher;

  public LocalCreateOrderSaga(DomainEventPublisher domainEventPublisher) {
    this.domainEventPublisher = domainEventPublisher;
  }

  @Autowired
  private OrderRepository orderRepository;

  private SagaDefinition<LocalCreateOrderSagaData> sagaDefinition =
          step()
            .invokeLocal(this::create)
            .withCompensation(this::reject)
          .step()
            .invokeParticipant(this::reserveCredit)
          .step()
            .invokeLocal(this::approve)
          .build();

  private void create(LocalCreateOrderSagaData data) {
    ResultWithEvents<Order> oe = Order.createOrder(data.getOrderDetails());
    Order order = oe.result;
    orderRepository.save(order);
    data.setOrderId(order.getId());
  }


  @Override
  public SagaDefinition<LocalCreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }


  private CommandWithDestination reserveCredit(LocalCreateOrderSagaData data) {
    long orderId = data.getOrderId();
    Long customerId = data.getOrderDetails().getCustomerId();
    Money orderTotal = data.getOrderDetails().getOrderTotal();
    return send(new ReserveCreditCommand(customerId, orderId, orderTotal))
            .to("customerService")
            .build();
  }

  public void reject(LocalCreateOrderSagaData data) {
    orderRepository.findById(data.getOrderId()).get().noteCreditReservationFailed();
  }

  private void approve(LocalCreateOrderSagaData data) {
    orderRepository.findById(data.getOrderId()).get().noteCreditReserved();
  }

  @Override
  public void onSagaCompletedSuccessfully(String sagaId, LocalCreateOrderSagaData createOrderSagaData) {
    domainEventPublisher.publish(LocalCreateOrderSaga.class, sagaId, Collections.singletonList(new CreateOrderSagaCompletedSuccesfully(createOrderSagaData.getOrderId())));
  }

  @Override
  public void onSagaRolledBack(String sagaId, LocalCreateOrderSagaData createOrderSagaData) {
    domainEventPublisher.publish(LocalCreateOrderSaga.class, sagaId, Collections.singletonList(new CreateOrderSagaRolledBack(createOrderSagaData.getOrderId())));
  }
}
