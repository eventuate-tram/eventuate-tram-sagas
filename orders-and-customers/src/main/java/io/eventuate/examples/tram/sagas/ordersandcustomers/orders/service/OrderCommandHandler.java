package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.Order;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ApproveOrderCommand;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.participant.SagaCommandHandlersBuilder;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

public class OrderCommandHandler {

  private OrderDao orderDao;

  public OrderCommandHandler(OrderDao orderDao) {
    this.orderDao = orderDao;
  }

  public CommandHandlers commandHandlerDefinitions() {
    return SagaCommandHandlersBuilder
            .fromChannel("orderService")
            .onMessage(ApproveOrderCommand.class, this::approve)
            .onMessage(RejectOrderCommand.class, this::reject)
            .build();
  }

  public Message approve(CommandMessage<ApproveOrderCommand> cm) {
    long orderId = cm.getCommand().getOrderId();
    Order order = orderDao.findById(orderId);
    order.noteCreditReserved();
    return withSuccess();
  }

  public Message reject(CommandMessage<RejectOrderCommand> cm) {
    long orderId = cm.getCommand().getOrderId();
    Order order = orderDao.findById(orderId);
    order.noteCreditReservationFailed();
    return withSuccess();
  }

}
