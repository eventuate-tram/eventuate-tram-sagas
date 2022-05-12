package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ReserveCreditCommand;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.events.publisher.ResultWithEvents;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

import java.util.Collections;

import static io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder.send;

public class LocalCreateOrderSaga implements SimpleSaga<LocalCreateOrderSagaData> {

  private DomainEventPublisher domainEventPublisher;
  private OrderDao orderDao;

  public LocalCreateOrderSaga(DomainEventPublisher domainEventPublisher,
                              OrderDao orderDao) {
    this.domainEventPublisher = domainEventPublisher;
    this.orderDao = orderDao;
  }

  private SagaDefinition<SagaActions<LocalCreateOrderSagaData>, LocalCreateOrderSagaData> sagaDefinition =
          step()
                  .invokeLocal(this::create)
                  .withCompensation(this::reject)
                  .step()
                  .invokeParticipant(this::reserveCredit)
                  .step()
                  .invokeLocal(this::approve)
                  .build();

  @Override
  public SagaDefinition<SagaActions<LocalCreateOrderSagaData>, LocalCreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }


  private void create(LocalCreateOrderSagaData data) {
    ResultWithEvents<Order> oe = Order.createOrder(data.getOrderDetails());
    Order order = oe.result;
    orderDao.save(order);
    data.setOrderId(order.getId());
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
    orderDao.findById(data.getOrderId()).noteCreditReservationFailed();
  }

  private void approve(LocalCreateOrderSagaData data) {
    orderDao.findById(data.getOrderId()).noteCreditReserved();
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
