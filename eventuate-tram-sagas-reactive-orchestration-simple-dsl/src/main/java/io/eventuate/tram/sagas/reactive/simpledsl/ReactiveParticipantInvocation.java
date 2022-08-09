package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;
import org.reactivestreams.Publisher;


public interface ReactiveParticipantInvocation<Data> {

  boolean isSuccessfulReply(Message message);

  boolean isInvocable(Data data);

  Publisher<CommandWithDestinationAndType> makeCommandToSend(Data data);
}
