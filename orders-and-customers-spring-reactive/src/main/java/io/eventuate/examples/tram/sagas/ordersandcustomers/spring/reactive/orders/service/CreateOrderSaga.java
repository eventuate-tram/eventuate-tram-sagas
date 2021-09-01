package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.service;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.common.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.commands.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.replies.CustomerCreditLimitExceeded;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.customers.replies.CustomerNotFound;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.common.RejectionReason;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.reactive.orders.createorder.CreateOrderSagaData;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;
import io.eventuate.tram.sagas.reactive.simpledsl.SimpleReactiveSaga;
import reactor.core.publisher.Mono;

import static io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder.send;

public class CreateOrderSaga implements SimpleReactiveSaga<CreateOrderSagaData> {

  private OrderService orderService;

  public CreateOrderSaga(OrderService orderService) {
    this.orderService = orderService;
  }

  private ReactiveSagaDefinition<CreateOrderSagaData> sagaDefinition =
          step()
            .invokeLocal(this::create)
            .withCompensation(this::reject)
          .step()
            .invokeParticipant(this::reserveCredit)
            .onReply(CustomerNotFound.class, this::handleCustomerNotFound)
            .onReply(CustomerCreditLimitExceeded.class, this::handleCustomerCreditLimitExceeded)
          .step()
            .invokeLocal(this::approve)
          .build();

  private Mono<Void> handleCustomerNotFound(CreateOrderSagaData data, CustomerNotFound reply) {
    return Mono.fromRunnable(() -> data.setRejectionReason(RejectionReason.UNKNOWN_CUSTOMER));
  }

  private Mono<Void> handleCustomerCreditLimitExceeded(CreateOrderSagaData data, CustomerCreditLimitExceeded reply) {
    return Mono.fromRunnable(() -> data.setRejectionReason(RejectionReason.INSUFFICIENT_CREDIT));
  }

  @Override
  public ReactiveSagaDefinition<CreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

  private Mono<Void> create(CreateOrderSagaData data) {
    return orderService
            .createOrder(data.getOrderDetails())
            .map(o -> {
              data.setOrderId(o.getId());
              return o;
            })
            .then();
  }

  private Mono<CommandWithDestination> reserveCredit(CreateOrderSagaData data) {
    Long customerId = data.getOrderDetails().getCustomerId();
    Money orderTotal = data.getOrderDetails().getOrderTotal();

    return Mono
            .fromSupplier(() -> send(new ReserveCreditCommand(customerId, data.getOrderId(), orderTotal))
                      .to("customerService")
                      .build());
  }

  private Mono<Void> approve(CreateOrderSagaData data) {
    return Mono.defer(() -> orderService.approveOrder(data.getOrderId()));
  }

  private Mono<Void> reject(CreateOrderSagaData data) {
    return Mono.defer(() -> orderService.rejectOrder(data.getOrderId(), data.getRejectionReason()));
  }
}
