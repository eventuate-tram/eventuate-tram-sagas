package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.messaging.common.Message;


public interface ParticipantInvocation<Data> {

  boolean isSuccessfulReply(Message message);

  boolean isInvocable(Data data);

  CommandWithDestination makeCommandToSend(Data data);
}
