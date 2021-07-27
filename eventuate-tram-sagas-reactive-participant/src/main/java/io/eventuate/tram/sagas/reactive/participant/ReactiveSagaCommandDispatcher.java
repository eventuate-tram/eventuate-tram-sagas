package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandHandlerParams;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandDispatcher;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandler;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.sagas.common.*;
import io.eventuate.tram.sagas.participant.SagaReplyMessage;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Optional;

public class ReactiveSagaCommandDispatcher extends ReactiveCommandDispatcher {

  private ReactiveSagaLockManager sagaLockManager;

  public ReactiveSagaCommandDispatcher(String commandDispatcherId,
                                       ReactiveCommandHandlers target,
                                       ReactiveMessageConsumer messageConsumer,
                                       ReactiveMessageProducer messageProducer,
                                       ReactiveSagaLockManager sagaLockManager) {
    super(commandDispatcherId, target, messageConsumer, messageProducer);

    this.sagaLockManager = sagaLockManager;
  }

  @Override
  public Publisher<?> messageHandler(Message message) {
    if (isUnlockMessage(message)) {
      String sagaId = getSagaId(message);
      String target = message.getRequiredHeader(CommandMessageHeaders.RESOURCE);
      return sagaLockManager.unlock(sagaId, target).map(m -> super.messageHandler(message));
    } else {
        return Mono
                .from(super.messageHandler(message))
                .map(o -> (Object) o)
                .onErrorResume(StashMessageRequiredException.class, e -> {
                  String sagaType = getSagaType(message);
                  String sagaId = getSagaId(message);
                  String target = e.getTarget();
                  return sagaLockManager.stashMessage(sagaType, sagaId, target, message);
                });
    }
  }

  private String getSagaId(Message message) {
    return message.getRequiredHeader(SagaCommandHeaders.SAGA_ID);
  }

  private String getSagaType(Message message) {
    return message.getRequiredHeader(SagaCommandHeaders.SAGA_TYPE);
  }


  @Override
  protected Publisher<Message> invoke(ReactiveCommandHandler commandHandler, CommandMessage cm, CommandHandlerParams commandHandlerParams) {
    Optional<String> lockedTarget = Optional.empty();

    Flux<Message> result = Flux.empty();

    if (commandHandler instanceof ReactiveSagaCommandHandler) {
      ReactiveSagaCommandHandler sch = (ReactiveSagaCommandHandler) commandHandler;
      if (sch.getPreLock().isPresent()) {
        LockTarget lockTarget = sch.getPreLock().get().apply(cm, new PathVariables(commandHandlerParams.getPathVars()));
        Message message = cm.getMessage();
        String sagaType = getSagaType(message);
        String sagaId = getSagaId(message);
        String target = lockTarget.getTarget();
        lockedTarget = Optional.of(target);

        result = Flux.from(sagaLockManager
                  .claimLock(sagaType, sagaId, target)
                  .flatMap(locked -> {
                    if (!locked) return Mono.error(new StashMessageRequiredException(target));
                    else return Mono.empty();
                  }));
      }
    }

    result = result.thenMany(super.invoke(commandHandler, cm, commandHandlerParams)).cache();
    Flux<Message> finalizedResult = result;

    if (lockedTarget.isPresent())
      return addLockedHeader(result, lockedTarget.get());
    else {
      Mono<LockTarget> lock = getLock(result);

      return Flux.from(lock).flatMap(lt ->
        finalizedResult.flatMap(msg -> {
          Message message = cm.getMessage();
          String sagaType = getSagaType(message);
          String sagaId = getSagaId(message);
          return Flux.from(sagaLockManager
                  .claimLock(sagaType, sagaId, lt.getTarget())
                  .flatMap(locked -> {
                    if (locked) return Mono.from(addLockedHeader(Flux.just(msg), lt.getTarget()));
                    else return Mono.error(new RuntimeException("Cannot claim lock"));
                  }));
        })).switchIfEmpty(finalizedResult);
    }
  }

  private Mono<LockTarget> getLock(Flux<Message> messages) {
    return messages
            .filter(m -> m instanceof SagaReplyMessage && ((SagaReplyMessage) m).hasLockTarget())
            .map(m -> ((SagaReplyMessage)m).getLockTarget().get())
            .next();
  }

  private Publisher<Message> addLockedHeader(Publisher<Message> messages, String lockedTarget) {
    return Flux.from(messages).map(msg -> MessageBuilder.withMessage(msg).withHeader(SagaReplyHeaders.REPLY_LOCKED, lockedTarget).build());
  }

  private boolean isUnlockMessage(Message message) {
    return message.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE).equals(SagaUnlockCommand.class.getName());
  }

}
