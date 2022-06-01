package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders;

import org.springframework.context.ApplicationEvent;

public class SagaLifecycleEvent extends ApplicationEvent {
    protected String sagaId;

    public SagaLifecycleEvent(Object source, String sagaId) {
        super(source);
        this.sagaId = sagaId;
    }

    public String getSagaId() {
        return sagaId;
    }
}
