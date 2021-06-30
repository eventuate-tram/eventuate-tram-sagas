package io.eventuate.tram.sagas.reactive.participant;


import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandler;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ReactiveSagaCommandHandlersBuilder implements AbstractReactiveSagaCommandHandlersBuilder {
  private String channel;

  private List<ReactiveCommandHandler> handlers = new ArrayList<>();

  public static ReactiveSagaCommandHandlersBuilder fromChannel(String channel) {
    return new ReactiveSagaCommandHandlersBuilder().andFromChannel(channel);
  }

  private ReactiveSagaCommandHandlersBuilder andFromChannel(String channel) {
    this.channel = channel;
    return this;
  }

  @Override
  public <C> ReactiveSagaCommandHandlerBuilder<C> onMessage(Class<C> commandClass,
                                                            Function<CommandMessage<C>, Publisher<Message>> handler) {
    ReactiveSagaCommandHandler h = new ReactiveSagaCommandHandler(channel, commandClass, handler);

    this.handlers.add(h);
    return new ReactiveSagaCommandHandlerBuilder<C>(this, h);
  }

  public ReactiveCommandHandlers build() {
    return new ReactiveCommandHandlers(handlers);
  }
}
