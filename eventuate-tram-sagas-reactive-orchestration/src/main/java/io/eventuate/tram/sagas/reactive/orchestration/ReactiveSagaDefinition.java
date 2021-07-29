package io.eventuate.tram.sagas.reactive.orchestration;


import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import org.reactivestreams.Publisher;

public interface ReactiveSagaDefinition<Data> {

  Publisher<SagaActions<Data>> start(Data sagaData);

  Publisher<SagaActions<Data>> handleReply(String currentState, Data sagaData, Message message);
}
