package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.common.Success;
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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
            .then(Mono.defer(() -> {
              String sagaId = sagaInstance.getId();
              return saga.onStarting(sagaId, sagaData).thenReturn(sagaId);
            }))
            .flatMap(sagaId -> {
              if (resource.isPresent()) {
                return sagaLockManager
                        .claimLock(getSagaType(), sagaId, resource.get())
                        .flatMap(blocked -> {
                          if (blocked) return Mono.empty();
                          else return Mono.error(new RuntimeException("Cannot claim lock for resource"));
                        });
              }
              return Mono.empty();
            })
            .then(Mono.defer(() -> Mono.from(getStateDefinition().start(sagaData))))
            .flatMap(actions -> {
              if (actions.getLocalException().isPresent()) return Mono.error(actions.getLocalException().get());
              else return Mono.just(actions);
            })
            .flatMap(actions -> processActions(sagaInstance.getId(), sagaInstance, sagaData, Mono.just(actions)))
            .then(Mono.fromSupplier(() -> sagaInstance));
  }


  private Mono<Void> performEndStateActions(String sagaId, SagaInstance sagaInstance, boolean compensating, Data sagaData) {
    List<Mono<String>> actions = new ArrayList<>();

    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
      Map<String, String> headers = new HashMap<>();
      headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
      headers.put(SagaCommandHeaders.SAGA_TYPE, getSagaType());
      actions.add(commandProducer.send(dr.getDestination(), dr.getResource(), new SagaUnlockCommand(), makeSagaReplyChannel(), headers));
    }

    return Flux.merge(actions).then(compensating ? saga.onSagaRolledBack(sagaId, sagaData) : saga.onSagaCompletedSuccessfully(sagaId, sagaData));
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


  @PostConstruct
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

              Mono<SagaActions<Data>> actions = Mono.from(getStateDefinition().handleReply(currentState, getSagaData(si), message));

              return processActions(sagaId, si, data, actions);
            })
            .then();
  }

  private Data getSagaData(SagaInstance sagaInstance) {
    return SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData());
  }

  private Mono<SagaActions<Data>> processActions(String sagaId, SagaInstance sagaInstance, Data sagaData, Mono<SagaActions<Data>> actions) {
    return actions.flatMap(acts -> {
      if (acts.getLocalException().isPresent()) {
        Mono<SagaActions<Data>> nextActions = Mono.from(getStateDefinition()
                .handleReply(
                        acts.getUpdatedState().get(),
                        acts.getUpdatedSagaData().get(),
                        MessageBuilder
                                .withPayload("{}")
                                .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, CommandReplyOutcome.FAILURE.name())
                                .withHeader(ReplyMessageHeaders.REPLY_TYPE, Failure.class.getName())
                                .build()
                ));

        return processActions(sagaId, sagaInstance, sagaData, nextActions);
      } else {
        Mono<SagaActions<Data>> nextActions = sagaCommandProducer
                .sendCommands(this.getSagaType(), sagaId, acts.getCommands(), this.makeSagaReplyChannel())
                .map(lastId -> {
                  sagaInstance.setLastRequestId(lastId);
                  updateState(sagaInstance, acts);
                  sagaInstance.setSerializedSagaData(SagaDataSerde.serializeSagaData(acts.getUpdatedSagaData().orElse(sagaData)));
                  if (acts.isEndState()) {
                    return performEndStateActions(sagaId, sagaInstance, acts.isCompensating(), sagaData).thenReturn(lastId);
                  }
                  return Mono.just(lastId);
                })
                .then(Mono.defer(() -> sagaInstanceRepository.update(sagaInstance)))
                .then(Mono.defer(() -> {
                  if (!acts.isLocal()) return Mono.empty();
                  else return Mono.just(acts);
                }))
                .flatMap(newActs ->
                  Mono.from(getStateDefinition()
                          .handleReply(newActs.getUpdatedState().get(),
                                  newActs.getUpdatedSagaData().get(),
                                  MessageBuilder
                                          .withPayload("{}")
                                          .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, CommandReplyOutcome.SUCCESS.name())
                                          .withHeader(ReplyMessageHeaders.REPLY_TYPE, Success.class.getName())
                                          .build())));

        return nextActions.flatMap(na -> processActions(sagaId, sagaInstance, sagaData, Mono.just(na)));
      }
    });
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
