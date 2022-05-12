package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.common.*;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singleton;

public class SagaManagerImpl<SAGA_DATA>
        implements SagaManager<SAGA_DATA> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private Saga<SAGA_DATA> saga;
  private SagaInstanceRepository sagaInstanceRepository;
  private CommandProducer commandProducer;
  private MessageConsumer messageConsumer;
  private SagaLockManager sagaLockManager;
  private SagaCommandProducer sagaCommandProducer;

  public SagaManagerImpl(Saga<SAGA_DATA> saga,
                         SagaInstanceRepository sagaInstanceRepository,
                         CommandProducer commandProducer,
                         MessageConsumer messageConsumer,
                         SagaLockManager sagaLockManager,
                         SagaCommandProducer sagaCommandProducer) {
    this.saga = saga;
    this.sagaInstanceRepository = sagaInstanceRepository;
    this.commandProducer = commandProducer;
    this.messageConsumer = messageConsumer;
    this.sagaLockManager = sagaLockManager;
    this.sagaCommandProducer = sagaCommandProducer;
  }

  public void setSagaCommandProducer(SagaCommandProducer sagaCommandProducer) {
    this.sagaCommandProducer = sagaCommandProducer;
  }

  public void setSagaInstanceRepository(SagaInstanceRepository sagaInstanceRepository) {
    this.sagaInstanceRepository = sagaInstanceRepository;
  }

  public void setCommandProducer(CommandProducer commandProducer) {
    this.commandProducer = commandProducer;
  }

  public void setMessageConsumer(MessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }


  public void setSagaLockManager(SagaLockManager sagaLockManager) {
    this.sagaLockManager = sagaLockManager;
  }

  @Override
  public SagaInstance create(SAGA_DATA sagaData) {
    return create(sagaData, Optional.empty());
  }

  @Override
  public SagaInstance create(SAGA_DATA data, Class targetClass, Object targetId) {
    return create(data, Optional.of(new LockTarget(targetClass, targetId).getTarget()));
  }

  @Override
  public SagaInstance create(SAGA_DATA sagaData, Optional<String> resource) {


    SagaInstance sagaInstance = new SagaInstance(getSagaType(),
            null,
            "????",
            null,
            SagaDataSerde.serializeSagaData(sagaData), new HashSet<>());

    sagaInstanceRepository.save(sagaInstance);

    String sagaId = sagaInstance.getId();

    saga.onStarting(sagaId, sagaData);

    resource.ifPresent(r -> {
      if (!sagaLockManager.claimLock(getSagaType(), sagaId, r)) {
        throw new RuntimeException("Cannot claim lock for resource");
      }
    });

    SagaActions<SAGA_DATA> actions = getStateDefinition().start(sagaData);

    actions.getLocalException().ifPresent(e -> {
      throw e;
    });

    processActions(sagaId, sagaInstance, sagaData, actions);

    return sagaInstance;
  }


  private void performEndStateActions(String sagaId, SagaInstance sagaInstance, boolean compensating, SAGA_DATA sagaData) {
    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
      Map<String, String> headers = new HashMap<>();
      headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
      headers.put(SagaCommandHeaders.SAGA_TYPE, getSagaType()); // FTGO SagaCommandHandler failed without this but the OrdersAndCustomersIntegrationTest was fine?!?
      commandProducer.send(dr.getDestination(), dr.getResource(), new SagaUnlockCommand(), makeSagaReplyChannel(), headers);
    }

    if (compensating)
      saga.onSagaRolledBack(sagaId, sagaData);
    else
      saga.onSagaCompletedSuccessfully(sagaId, sagaData);

  }

  private SagaDefinition<SagaActions<SAGA_DATA>, SAGA_DATA> getStateDefinition() {
    SagaDefinition<SagaActions<SAGA_DATA>, SAGA_DATA> sm = saga.getSagaDefinition();

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


  public void handleMessage(Message message) {
    logger.debug("handle message invoked {}", message);
    if (message.hasHeader(SagaReplyHeaders.REPLY_SAGA_ID)) {
      handleReply(message);
    } else {
      logger.warn("Handle message doesn't know what to do with: {} ", message);
    }
  }

  private void handleReply(Message message) {

    if (!isReplyForThisSagaType(message))
      return;

    logger.debug("Handle reply: {}", message);

    String sagaId = message.getRequiredHeader(SagaReplyHeaders.REPLY_SAGA_ID);
    String sagaType = message.getRequiredHeader(SagaReplyHeaders.REPLY_SAGA_TYPE);

    SagaInstance sagaInstance = sagaInstanceRepository.find(sagaType, sagaId);
    SAGA_DATA sagaData = SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData());


    message.getHeader(SagaReplyHeaders.REPLY_LOCKED).ifPresent(lockedTarget -> {
      String destination = message.getRequiredHeader(CommandMessageHeaders.inReply(CommandMessageHeaders.DESTINATION));
      sagaInstance.addDestinationsAndResources(singleton(new DestinationAndResource(destination, lockedTarget)));
    });

    String currentState = sagaInstance.getStateName();

    logger.info("Current state={}", currentState);

    SagaActions<SAGA_DATA> actions = getStateDefinition().handleReply(currentState, sagaData, message);

    logger.info("Handled reply. Sending commands {}", actions.getCommands());

    processActions(sagaId, sagaInstance, sagaData, actions);
  }

  private void processActions(String sagaId, SagaInstance sagaInstance, SAGA_DATA sagaData, SagaActions<SAGA_DATA> actions) {


    while (true) {

      if (actions.getLocalException().isPresent()) {

        actions = getStateDefinition().handleReply(actions.getUpdatedState().get(), actions.getUpdatedSagaData().get(), MessageBuilder
                .withPayload("{}")
                .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, CommandReplyOutcome.FAILURE.name())
                .withHeader(ReplyMessageHeaders.REPLY_TYPE, Failure.class.getName())
                .build());


      } else {
        // only do this if successful

        String lastRequestId = sagaCommandProducer.sendCommands(this.getSagaType(), sagaId, actions.getCommands(), this.makeSagaReplyChannel());
        sagaInstance.setLastRequestId(lastRequestId);

        updateState(sagaInstance, actions);

        sagaInstance.setSerializedSagaData(SagaDataSerde.serializeSagaData(actions.getUpdatedSagaData().orElse(sagaData)));

        if (actions.isEndState()) {
          performEndStateActions(sagaId, sagaInstance, actions.isCompensating(), sagaData);
        }

        sagaInstanceRepository.update(sagaInstance);

        if (!actions.isLocal())
          break;

        actions = getStateDefinition().handleReply(actions.getUpdatedState().get(), actions.getUpdatedSagaData().get(), MessageBuilder
                .withPayload("{}")
                .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, CommandReplyOutcome.SUCCESS.name())
                .withHeader(ReplyMessageHeaders.REPLY_TYPE, Success.class.getName())
                .build());
      }
    }
  }

  private void updateState(SagaInstance sagaInstance, SagaActions<SAGA_DATA> actions) {
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
