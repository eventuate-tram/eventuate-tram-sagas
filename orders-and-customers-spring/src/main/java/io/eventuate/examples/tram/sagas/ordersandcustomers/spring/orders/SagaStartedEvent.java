package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders;

public class SagaStartedEvent extends SagaLifecycleEvent {

    public SagaStartedEvent(Object source, String sagaId) {
        super(source, sagaId);
    }


}
