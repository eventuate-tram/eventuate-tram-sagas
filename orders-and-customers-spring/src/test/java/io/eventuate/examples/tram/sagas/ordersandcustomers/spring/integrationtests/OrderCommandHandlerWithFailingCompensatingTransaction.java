package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.domain.OrderDao;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ApproveOrderCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.OrderCommandHandler;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.RejectOrderCommand;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.participant.SagaCommandHandlersBuilder;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withFailure;

class OrderCommandHandlerWithFailingCompensatingTransaction extends OrderCommandHandler {
    public OrderCommandHandlerWithFailingCompensatingTransaction(OrderDao orderDao) {
        super(orderDao);
    }

    @Override
    public CommandHandlers commandHandlerDefinitions() {
        return SagaCommandHandlersBuilder
                .fromChannel("orderService")
                .onMessage(ApproveOrderCommand.class, this::approve)
                .onMessage(RejectOrderCommand.class, this::reject)
                .build();
    }

    public Message reject(CommandMessage<RejectOrderCommand> cm) {
        return withFailure();
    }
}
