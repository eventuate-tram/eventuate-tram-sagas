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
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
  protected Publisher<List<Message>> invoke(ReactiveCommandHandler commandHandler, CommandMessage cm, CommandHandlerParams commandHandlerParams) {
    Optional<String> lockedTarget = Optional.empty();

    Mono<List<Message>> result = Mono.just(Collections.emptyList());

    if (commandHandler instanceof ReactiveSagaCommandHandler) {
      ReactiveSagaCommandHandler sch = (ReactiveSagaCommandHandler) commandHandler;
      if (sch.getPreLock().isPresent()) {
        LockTarget lockTarget = sch.getPreLock().get().apply(cm, new PathVariables(commandHandlerParams.getPathVars()));
        Message message = cm.getMessage();
        String sagaType = getSagaType(message);
        String sagaId = getSagaId(message);
        String target = lockTarget.getTarget();
        lockedTarget = Optional.of(target);

        result = result.flatMap(messages ->
          sagaLockManager
                  .claimLock(sagaType, sagaId, target)
                  .flatMap(locked -> {
                    if (!locked) return Mono.error(new StashMessageRequiredException(target));
                    else return Mono.just(messages);
                  }));
      }
    }

    result = result.flatMap(messages -> Mono.from(super.invoke(commandHandler, cm, commandHandlerParams)));

    if (lockedTarget.isPresent())
      return addLockedHeader(result, lockedTarget.get());
    else {
      Mono<LockTarget> lock = getLock(result);

      return result.flatMap(messages ->
        lock
                .flatMap(lt -> {
                  Message message = cm.getMessage();
                  String sagaType = getSagaType(message);
                  String sagaId = getSagaId(message);
                  return sagaLockManager
                          .claimLock(sagaType, sagaId, lt.getTarget())
                          .flatMap(locked -> {
                            if (locked) return addLockedHeader(Mono.just(messages), lt.getTarget());
                            else return Mono.error(new RuntimeException("Cannot claim lock"));
                          });
                })
                .switchIfEmpty(Mono.just(messages)));
    }
  }

  private Mono<LockTarget> getLock(Mono<List<Message>> messages) {
    return messages.flatMap(msgs -> {
      Optional<LockTarget> lockTarget = msgs
              .stream()
              .filter(m -> m instanceof SagaReplyMessage && ((SagaReplyMessage) m).hasLockTarget())
              .findFirst()
              .flatMap(m -> ((SagaReplyMessage)m).getLockTarget());

      return Mono.justOrEmpty(lockTarget);
    });
  }

  private Mono<List<Message>> addLockedHeader(Mono<List<Message>> messages, String lockedTarget) {
    return messages
            .map(msgs -> msgs
                    .stream()
                    .map(m -> MessageBuilder.withMessage(m).withHeader(SagaReplyHeaders.REPLY_LOCKED, lockedTarget).build())
                    .collect(Collectors.toList()));
  }

  private boolean isUnlockMessage(Message message) {
    return message.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE).equals(SagaUnlockCommand.class.getName());
  }

}
