package io.eventuate.tram.sagas.orchestration;


import io.eventuate.tram.messaging.common.Message;

public interface SagaDefinition<SAGA_ACTIONS, SAGA_DATA> {

  SAGA_ACTIONS start(SAGA_DATA sagaData);

  SAGA_ACTIONS handleReply(String currentState, SAGA_DATA sagaData, Message message);
}
