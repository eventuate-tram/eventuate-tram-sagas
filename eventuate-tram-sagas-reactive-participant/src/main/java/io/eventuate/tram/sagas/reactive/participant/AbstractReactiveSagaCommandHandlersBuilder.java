package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import org.reactivestreams.Publisher;

import java.util.function.Function;

public interface AbstractReactiveSagaCommandHandlersBuilder {
  <C> ReactiveSagaCommandHandlerBuilder<C> onMessage(Class<C> commandClass,
                                                     Function<CommandMessage<C>, Publisher<Message>> handler);
}
