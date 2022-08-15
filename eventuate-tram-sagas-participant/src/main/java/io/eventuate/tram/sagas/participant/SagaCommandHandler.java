package io.eventuate.tram.sagas.participant;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandHandler;
import io.eventuate.tram.commands.consumer.CommandHandlerArgs;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.common.LockTarget;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SagaCommandHandler extends CommandHandler {


  private Optional<BiFunction<CommandMessage, PathVariables, LockTarget>> preLock = Optional.empty();
  private Optional<PostLockFunction> postLock = Optional.empty();

  public <C extends Command> SagaCommandHandler(String channel, Class<C> commandClass, Function<CommandHandlerArgs<C>, List<Message>> handler) {
    super(channel, Optional.empty(), commandClass, handler);
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
