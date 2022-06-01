package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders;

public class SagaFailedEvent extends SagaLifecycleEvent {

    public SagaFailedEvent(Object source, String sagaId) {
        super(source, sagaId);
    }


}
