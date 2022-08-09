package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;


public interface ParticipantInvocation<Data> {

  boolean isSuccessfulReply(Message message);

  boolean isInvocable(Data data);

  CommandWithDestinationAndType makeCommandToSend(Data data);
}
