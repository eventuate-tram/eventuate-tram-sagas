package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.tram.commands.common.*;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducer;
import io.eventuate.tram.sagas.common.LockTarget;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;
import io.eventuate.tram.sagas.common.SagaReplyHeaders;
import io.eventuate.tram.sagas.common.SagaUnlockCommand;
import io.eventuate.tram.sagas.orchestration.DestinationAndResource;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDataSerde;
import io.eventuate.tram.sagas.orchestration.SagaInstance;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singleton;

public class ReactiveSagaManagerImpl<Data>
        implements ReactiveSagaManager<Data> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private ReactiveSaga<Data> saga;
  private ReactiveSagaInstanceRepository sagaInstanceRepository;
  private ReactiveCommandProducer commandProducer;
  private ReactiveMessageConsumer messageConsumer;
  private ReactiveSagaLockManager sagaLockManager;
  private ReactiveSagaCommandProducer sagaCommandProducer;

  public ReactiveSagaManagerImpl(ReactiveSaga<Data> saga,
                                 ReactiveSagaInstanceRepository sagaInstanceRepository,
                                 ReactiveCommandProducer commandProducer,
                                 ReactiveMessageConsumer messageConsumer,
                                 ReactiveSagaLockManager sagaLockManager,
                                 ReactiveSagaCommandProducer sagaCommandProducer) {
    this.saga = saga;
    this.sagaInstanceRepository = sagaInstanceRepository;
    this.commandProducer = commandProducer;
    this.messageConsumer = messageConsumer;
    this.sagaLockManager = sagaLockManager;
    this.sagaCommandProducer = sagaCommandProducer;
  }

  public void setSagaCommandProducer(ReactiveSagaCommandProducer sagaCommandProducer) {
    this.sagaCommandProducer = sagaCommandProducer;
  }

  public void setSagaInstanceRepository(ReactiveSagaInstanceRepository sagaInstanceRepository) {
    this.sagaInstanceRepository = sagaInstanceRepository;
  }

  public void setCommandProducer(ReactiveCommandProducer commandProducer) {
    this.commandProducer = commandProducer;
  }

  public void setMessageConsumer(ReactiveMessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }


  public void setSagaLockManager(ReactiveSagaLockManager sagaLockManager) {
    this.sagaLockManager = sagaLockManager;
  }

  @Override
  public Mono<SagaInstance> create(Data sagaData) {
    return create(sagaData, Optional.empty());
  }

  @Override
  public Mono<SagaInstance> create(Data data, Class targetClass, Object targetId) {
    return create(data, Optional.of(new LockTarget(targetClass, targetId).getTarget()));
  }

  @Override
  public Mono<SagaInstance> create(Data sagaData, Optional<String> resource) {
    SagaInstance sagaInstance = new SagaInstance(getSagaType(),
            null,
            "????",
            null,
            SagaDataSerde.serializeSagaData(sagaData), new HashSet<>());

    return sagaInstanceRepository
            .save(sagaInstance)
            .then(Mono.defer(() -> saga.onStarting(sagaInstance.getId(), sagaData).thenReturn(sagaInstance.getId())))
            .flatMap(sagaId -> resource.map(s -> sagaLockManager
                    .claimLock(getSagaType(), sagaId, s)
                    .flatMap(blocked -> {
                      if (blocked) return Mono.empty();
                      else return Mono.error(new RuntimeException("Cannot claim lock for resource"));
                    })).orElseGet(Mono::empty))
            .then(Mono.defer(() -> Mono.from(getStateDefinition().start(sagaData))))
            .flatMap(actions -> actions.getLocalException()
                    .map(Mono::<SagaActions<Data>>error)
                    .orElseGet(() -> processActions(getSagaType(), sagaInstance.getId(), sagaInstance, sagaData, actions)))
            .then(Mono.just(sagaInstance));
  }


  private Mono<Void> performEndStateActions(String sagaId, SagaInstance sagaInstance, boolean compensating, Data sagaData) {
    return Flux.fromIterable(sagaInstance.getDestinationsAndResources())
            .map( dr -> {
              Map<String, String> headers = new HashMap<>();
              headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
              headers.put(SagaCommandHeaders.SAGA_TYPE, getSagaType());
              return commandProducer.send(dr.getDestination(), dr.getResource(), new SagaUnlockCommand(), makeSagaReplyChannel(), headers);
            })
            .then(Mono.defer(() -> compensating ? saga.onSagaRolledBack(sagaId, sagaData) : saga.onSagaCompletedSuccessfully(sagaId, sagaData)));
  }

  private ReactiveSagaDefinition<Data> getStateDefinition() {
    ReactiveSagaDefinition<Data> sm = saga.getSagaDefinition();

    if (sm == null) {
      throw new RuntimeException("state machine cannot be null");
    }

    return sm;
  }

  private String getSagaType() {
    return saga.getSagaType();
  }

  public void subscribeToReplyChannel() {
    messageConsumer.subscribe(saga.getSagaType() + "-consumer", singleton(makeSagaReplyChannel()),
            this::handleMessage);
  }

  private String makeSagaReplyChannel() {
    return getSagaType() + "-reply";
  }

  public Publisher<Void> handleMessage(Message message) {
    logger.debug("handle message invoked {}", message);
    if (message.hasHeader(SagaReplyHeaders.REPLY_SAGA_ID)) {
      return handleReply(message);
    } else {
      logger.warn("Handle message doesn't know what to do with: {} ", message);
      return Mono.empty();
    }
  }

  private Mono<Void> handleReply(Message message) {

    if (!isReplyForThisSagaType(message))
      return Mono.empty();

    logger.debug("Handle reply: {}", message);

    String sagaId = message.getRequiredHeader(SagaReplyHeaders.REPLY_SAGA_ID);
    String sagaType = message.getRequiredHeader(SagaReplyHeaders.REPLY_SAGA_TYPE);

    Mono<SagaInstance> sagaInstance = sagaInstanceRepository.find(sagaType, sagaId);

    return sagaInstance
            .map(si -> {
              message.getHeader(SagaReplyHeaders.REPLY_LOCKED).ifPresent(lockedTarget -> {
                String destination = message.getRequiredHeader(CommandMessageHeaders.inReply(CommandMessageHeaders.DESTINATION));
                si.addDestinationsAndResources(singleton(new DestinationAndResource(destination, lockedTarget)));
              });

              return si;
            })
            .flatMap(si -> {
              String currentState = si.getStateName();

              Data data = SagaDataSerde.deserializeSagaData(si.getSerializedSagaData());

              Mono<SagaActions<Data>> actions = Mono.from(getStateDefinition().handleReply(sagaType, sagaId, currentState, getSagaData(si), message));

              return processActions(sagaType, sagaId, si, data, actions);
            })
            .then();
  }

  private Data getSagaData(SagaInstance sagaInstance) {
    return SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData());
  }

  private Mono<SagaActions<Data>> processActions(String sagaType, String sagaId, SagaInstance sagaInstance, Data sagaData, Mono<SagaActions<Data>> actionsMono) {
    return actionsMono.flatMap(actions -> processActions(sagaType, sagaId, sagaInstance, sagaData, actions));
  }

  private Mono<SagaActions<Data>> processActions(String sagaType, String sagaId, SagaInstance sagaInstance, Data sagaData, SagaActions<Data> actions) {
    if (actions.getLocalException().isPresent()) {
      return simulateFailedReplyMessageForFailedLocalStep(sagaType, sagaId, sagaInstance, sagaData, actions);
    } else {
      Mono<SagaActions<Data>> nextActions = sagaCommandProducer
              .sendCommands(this.getSagaType(), sagaId, actions.getCommands(), this.makeSagaReplyChannel())
              .map(Optional::of)
              .switchIfEmpty(Mono.just(Optional.empty()))
              .map(lastId -> {
                lastId.ifPresent(sagaInstance::setLastRequestId);
                updateState(sagaInstance, actions);
                sagaInstance.setSerializedSagaData(SagaDataSerde.serializeSagaData(actions.getUpdatedSagaData().orElse(sagaData)));
                if (actions.isEndState()) {
                  return performEndStateActions(sagaId, sagaInstance, actions.isCompensating(), sagaData)
                     .then();
                } else
                  return Mono.empty();
              })
              .then(Mono.defer(() -> sagaInstanceRepository.update(sagaInstance)))
              .then(Mono.defer(() -> actions.isReplyExpected() ? Mono.empty() : simulateSuccessfulReplyToLocalActionOrNotification(sagaType, sagaId, actions)));
      return processActions(sagaType, sagaId, sagaInstance, sagaData, nextActions);
    }
  }

  private Mono<SagaActions<Data>> simulateSuccessfulReplyToLocalActionOrNotification(String sagaType, String sagaId, SagaActions<Data> actions) {
    return Mono.from(getStateDefinition()
            .handleReply(sagaType, sagaId, actions.getUpdatedState().get(),
                    actions.getUpdatedSagaData().get(),
                    MessageBuilder
                            .withPayload("{}")
                            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, CommandReplyOutcome.SUCCESS.name())
                            .withHeader(ReplyMessageHeaders.REPLY_TYPE, Success.class.getName())
                            .build()));
  }

  private Mono<SagaActions<Data>> simulateFailedReplyMessageForFailedLocalStep(String sagaType, String sagaId, SagaInstance sagaInstance, Data sagaData, SagaActions<Data> acts) {
    Mono<SagaActions<Data>> nextActions = Mono.from(getStateDefinition()
            .handleReply(
                    sagaType, sagaId, acts.getUpdatedState().get(),
                    acts.getUpdatedSagaData().get(),
                    MessageBuilder
                            .withPayload("{}")
                            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, CommandReplyOutcome.FAILURE.name())
                            .withHeader(ReplyMessageHeaders.REPLY_TYPE, Failure.class.getName())
                            .build()
            ));

    return processActions(sagaType, sagaId, sagaInstance, sagaData, nextActions);
  }

  private void updateState(SagaInstance sagaInstance, SagaActions<Data> actions) {
    actions.getUpdatedState().ifPresent(stateName -> {
      sagaInstance.setStateName(stateName);
      sagaInstance.setEndState(actions.isEndState());
      sagaInstance.setCompensating(actions.isCompensating());
    });
  }

  private Boolean isReplyForThisSagaType(Message message) {
    return message.getHeader(SagaReplyHeaders.REPLY_SAGA_TYPE).map(x -> x.equals(getSagaType())).orElse(false);
  }
}
