package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;


public interface ReactiveParticipantInvocation<Data> {

  boolean isSuccessfulReply(Message message);

  boolean isInvocable(Data data);

  Publisher<CommandWithDestination> makeCommandToSend(Data data);
}
