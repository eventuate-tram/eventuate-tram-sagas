package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.messaging.common.Message;

public interface SagaDefinition<Data> {

    SagaActions<Data> getActions(final Data sagaData);

    SagaActions<Data> getReplyActions(final String currentState, final Data sagaData, Message message);

}
