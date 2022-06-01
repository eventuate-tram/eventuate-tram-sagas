package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.messaging.common.Message;

public interface SagaDefinition<Data> {

  SagaActions<Data> start(Data sagaData);

  SagaActions<Data> handleReply(String sagaType, String sagaId, String currentState, Data sagaData, Message message);

}
