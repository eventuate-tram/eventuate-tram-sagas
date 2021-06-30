package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandler;
import io.eventuate.tram.sagas.common.LockTarget;
import io.eventuate.tram.sagas.participant.PostLockFunction;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ReactiveSagaCommandHandler extends ReactiveCommandHandler {


  private Optional<BiFunction<CommandMessage, PathVariables, LockTarget>> preLock = Optional.empty();
  private Optional<PostLockFunction> postLock = Optional.empty();

  public <C> ReactiveSagaCommandHandler(String channel, String resource, Class<C> commandClass, BiFunction<CommandMessage<C>, PathVariables, Publisher<Message>> handler) {
    super(channel, Optional.of(resource), commandClass, handler);
  }

  public <C> ReactiveSagaCommandHandler(String channel, Class<C> commandClass, Function<CommandMessage<C>, Publisher<Message>> handler) {
    super(channel, Optional.empty(), commandClass, (c, pv) -> handler.apply(c));
  }

  public void setPreLock(BiFunction<CommandMessage, PathVariables, LockTarget> preLock) {
    this.preLock = Optional.of(preLock);
  }

  public void setPostLock(PostLockFunction postLock) {
    this.postLock = Optional.of(postLock);
  }

  public Optional<BiFunction<CommandMessage, PathVariables, LockTarget>> getPreLock() {
    return preLock;
  }

  public Optional<PostLockFunction> getPostLock() {
    return postLock;
  }
}
