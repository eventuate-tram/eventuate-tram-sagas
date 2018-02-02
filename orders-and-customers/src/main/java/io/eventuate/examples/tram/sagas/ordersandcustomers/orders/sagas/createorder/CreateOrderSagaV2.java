package io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.createorder;

import io.eventuate.examples.tram.sagas.ordersandcustomers.commondomain.Money;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ApproveOrderCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.ReserveCreditCommand;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.proxy.CustomerServiceProxy;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.sagas.participants.proxy.OrderServiceProxy;
import io.eventuate.examples.tram.sagas.ordersandcustomers.orders.service.RejectOrderCommand;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

public class CreateOrderSagaV2 implements SimpleSaga<CreateOrderSagaData> {

  private CustomerServiceProxy customerService;
  private OrderServiceProxy orderService;

  public CreateOrderSagaV2(CustomerServiceProxy customerService, OrderServiceProxy orderService) {
    this.customerService = customerService;
    this.orderService = orderService;
  }

  private SagaDefinition<CreateOrderSagaData> sagaDefinition =
          step()
            .withCompensation(orderService.reject, this::makeRejectCommand)
          .step()
            .invokeParticipant(customerService.reserveCredit, this::makeReserveCreditCommand)
          .step()
            .invokeParticipant(orderService.approve, this::makeApproveCommand)
          .build();


  @Override
  public SagaDefinition<CreateOrderSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }


  private ReserveCreditCommand makeReserveCreditCommand(CreateOrderSagaData data) {
    long orderId = data.getOrderId();
    Long customerId = data.getOrderDetails().getCustomerId();
    Money orderTotal = data.getOrderDetails().getOrderTotal();
    return new ReserveCreditCommand(customerId, orderId, orderTotal);
  }

  private RejectOrderCommand makeRejectCommand(CreateOrderSagaData data) {
    return new RejectOrderCommand(data.getOrderId());
  }

  private ApproveOrderCommand makeApproveCommand(CreateOrderSagaData data) {
    return new ApproveOrderCommand(data.getOrderId());
  }


}
