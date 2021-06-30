package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.sagas.common.LockTarget;
import io.eventuate.tram.sagas.participant.PostLockFunction;
import org.reactivestreams.Publisher;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ReactiveSagaCommandHandlerBuilder<C> implements AbstractReactiveSagaCommandHandlersBuilder {
  private final ReactiveSagaCommandHandlersBuilder parent;
  private final ReactiveSagaCommandHandler h;

  public ReactiveSagaCommandHandlerBuilder(ReactiveSagaCommandHandlersBuilder parent, ReactiveSagaCommandHandler h) {
    super();
    this.parent = parent;
    this.h = h;
  }

  @Override
  public <C> ReactiveSagaCommandHandlerBuilder<C> onMessage(Class<C> commandClass, Function<CommandMessage<C>, Publisher<Message>> handler) {
    return parent.onMessage(commandClass, handler);
  }

  public ReactiveSagaCommandHandlerBuilder<C> withPreLock(BiFunction<CommandMessage<C>, PathVariables, LockTarget> preLock) {
    h.setPreLock(preLock::apply);
    return this;
  }

  public ReactiveSagaCommandHandlerBuilder<C> withPostLock(PostLockFunction<C> postLock) {
    h.setPostLock(postLock::apply);
    return this;
  }

  public ReactiveCommandHandlers build() {
    return parent.build();
  }
}
