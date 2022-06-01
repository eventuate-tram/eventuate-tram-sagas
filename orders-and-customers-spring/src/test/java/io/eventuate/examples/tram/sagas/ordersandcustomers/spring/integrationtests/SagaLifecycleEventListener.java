package io.eventuate.examples.tram.sagas.ordersandcustomers.spring.integrationtests;

import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders.SagaLifecycleEvent;
import io.eventuate.examples.tram.sagas.ordersandcustomers.spring.orders.SagaStartedEvent;
import org.springframework.context.event.EventListener;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class SagaLifecycleEventListener {

    private LinkedBlockingDeque<SagaLifecycleEvent> events = new LinkedBlockingDeque<>();

    @EventListener(classes = {SagaStartedEvent.class, SagaStartedEvent.class})
    public void handleSagaStartedEvent(SagaLifecycleEvent event) {
        events.add(event);
    }

    public void clear() {
        events.clear();
    }

    public <T extends SagaLifecycleEvent> T expectEvent(Class<T> eventClass) {
        try {
            return (T) events.poll(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
