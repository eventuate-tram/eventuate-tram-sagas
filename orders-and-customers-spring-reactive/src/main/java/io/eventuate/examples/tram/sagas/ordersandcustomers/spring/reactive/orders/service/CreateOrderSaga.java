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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static io.eventuate.tram.commands.consumer.CommandWithDestinationBuilder.send;

public class CreateOrderSaga implements SimpleReactiveSaga<CreateOrderSagaData> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final OrderService orderService;

  public CreateOrderSaga(OrderService orderService) {
    this.orderService = orderService;
  }

  private final ReactiveSagaDefinition<CreateOrderSagaData> sagaDefinition =
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
    data.setRejectionReason(RejectionReason.UNKNOWN_CUSTOMER);
    return Mono.empty();
  }

  private Mono<Void> handleCustomerCreditLimitExceeded(CreateOrderSagaData data, CustomerCreditLimitExceeded reply) {
    data.setRejectionReason(RejectionReason.INSUFFICIENT_CREDIT);
    return Mono.empty();
  }

  @Override
  public ReactiveSagaDefinition<CreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

  private Mono<?> create(CreateOrderSagaData data) {
    return orderService
            .createOrder(data.getOrderDetails())
            .map(o -> {
              logger.info("saga createOrder() with ID={}", o.getId());
              data.setOrderId(o.getId());
              return o;
            });
  }

  private Mono<CommandWithDestination> reserveCredit(CreateOrderSagaData data) {
    Long customerId = data.getOrderDetails().getCustomerId();
    Money orderTotal = data.getOrderDetails().getOrderTotal();

    return Mono
            .just(send(new ReserveCreditCommand(customerId, data.getOrderId(), orderTotal))
                    .to("customerService")
                    .build());
  }

  private Mono<Void> approve(CreateOrderSagaData data) {
    return orderService.approveOrder(data.getOrderId());
  }

  private Mono<Void> reject(CreateOrderSagaData data) {
    return orderService.rejectOrder(data.getOrderId(), data.getRejectionReason());
  }
}
