package io.eventuate.tram.sagas.orchestration;

import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.common.LockTarget;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;
import io.eventuate.tram.sagas.common.SagaReplyHeaders;
import io.eventuate.tram.sagas.common.SagaUnlockCommand;
import io.eventuate.tram.sagas.participant.SagaLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.*;

import static java.util.Collections.singleton;

@Component
public class SagaManagerImpl<Data>
        implements SagaManager<Data> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private SagaInstanceRepository sagaInstanceRepository;

  @Autowired
  private CommandProducer commandProducer;

  @Autowired
  private MessageConsumer messageConsumer;


  @Autowired
  private ChannelMapping channelMapping;

  private Saga<Data> saga;

  public SagaManagerImpl(Saga<Data> saga) {
    this.saga = saga;
  }

  public SagaManagerImpl(Saga<Data> saga, SagaInstanceRepository sagaInstanceRepository, CommandProducer
          commandProducer, MessageConsumer messageConsumer, ChannelMapping channelMapping,
                         SagaLockManager sagaLockManager, SagaCommandProducer sagaCommandProducer) {
    this.saga = saga;
    this.sagaInstanceRepository = sagaInstanceRepository;
    this.commandProducer = commandProducer;
    this.messageConsumer = messageConsumer;
    this.channelMapping = channelMapping;
    this.sagaLockManager = sagaLockManager;
    this.sagaCommandProducer = sagaCommandProducer;
  }



  @Autowired
  private SagaLockManager sagaLockManager;


  public void setSagaCommandProducer(SagaCommandProducer sagaCommandProducer) {
    this.sagaCommandProducer = sagaCommandProducer;
  }

  @Autowired
  private SagaCommandProducer sagaCommandProducer;



  public void setSagaInstanceRepository(SagaInstanceRepository sagaInstanceRepository) {
    this.sagaInstanceRepository = sagaInstanceRepository;
  }

  public void setCommandProducer(CommandProducer commandProducer) {
    this.commandProducer = commandProducer;
  }

  public void setMessageConsumer(MessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
  }


  public void setChannelMapping(ChannelMapping channelMapping) {
    this.channelMapping = channelMapping;
  }

  public void setSagaLockManager(SagaLockManager sagaLockManager) {
    this.sagaLockManager = sagaLockManager;
  }

  public void setDomainEventPublisher(DomainEventPublisher domainEventPublisher) {
  }

  @Override
  public SagaInstance create(Data sagaData) {
    return create(sagaData, Optional.empty());
  }

  @Override
  public SagaInstance create(Data data, Class targetClass, Object targetId) {
    return create(data, Optional.of(new LockTarget(targetClass, targetId).getTarget()));
  }

  @Override
  public SagaInstance create(Data sagaData, Optional<String> resource) {


    SagaInstance sagaInstance = new SagaInstance(getSagaType(),
            null,
            "????",
            null,
            SagaDataSerde.serializeSagaData(sagaData), new HashSet<>());

    sagaInstanceRepository.save(sagaInstance);

    String sagaId = sagaInstance.getId();

    resource.ifPresent( r -> Assert.isTrue(sagaLockManager.claimLock(getSagaType(), sagaId, r), "Cannot claim lock for resource"));

    SagaActions<Data> actions = getStateDefinition().invokeStartingHandler(sagaData);

    processActions(sagaId, sagaInstance, sagaData, actions);

    return sagaInstance;
  }



  private void performEndStateActions(String sagaId, SagaInstance sagaInstance) {
    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
      Map<String, String> headers = new HashMap<>();
      headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
      headers.put(SagaCommandHeaders.SAGA_TYPE, getSagaType()); // FTGO SagaCommandHandler failed without this but the OrdersAndCustomersIntegrationTest was fine?!?
      commandProducer.send(dr.getDestination(), dr.getResource(), new SagaUnlockCommand(), makeSagaReplyChannel(), headers);
    }
  }

  private SagaDefinition<Data> getStateDefinition() {
    SagaDefinition<Data> sm = saga.getSagaDefinition();
    Assert.notNull(sm, "state machine cannot be null");
    return sm;
  }

  private String getSagaType() {
    return saga.getSagaType();
  }


  @PostConstruct
  public void subscribeToReplyChannel() {
    messageConsumer.subscribe(saga.getSagaType() + "-consumer", singleton(channelMapping.transform(makeSagaReplyChannel())),
            this::handleMessage);
  }

  private String makeSagaReplyChannel() {
    return getSagaType() + "-reply";
  }


  public void handleMessage(Message message) {
    logger.debug("handle message invoked {}", message);
    if (message.hasHeader(SagaReplyHeaders.REPLY_SAGA_ID)) {
      handleReply(message);
    }  else {
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
    Data sagaData = SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData());


    message.getHeader(SagaReplyHeaders.REPLY_LOCKED).ifPresent(lockedTarget -> {
      String destination = message.getRequiredHeader(CommandMessageHeaders.inReply(CommandMessageHeaders.DESTINATION));
      sagaInstance.addDestinationsAndResources(singleton(new DestinationAndResource(destination, lockedTarget)));
    });

    String currentState = sagaInstance.getStateName();

    logger.info("Current state={}", currentState);
    SagaActions<Data> actions = getStateDefinition().handleReply(currentState, sagaData, message);

    logger.info("Handled reply. Sending commands {}", actions.getCommands());

    processActions(sagaId, sagaInstance, sagaData, actions);

  }

  private void processActions(String sagaId, SagaInstance sagaInstance, Data sagaData, SagaActions<Data> actions) {

    String lastRequestId = sagaCommandProducer.sendCommands(this.getSagaType(), sagaId, actions.getCommands(), this.makeSagaReplyChannel());
    sagaInstance.setLastRequestId(lastRequestId);

    actions.getUpdatedState().ifPresent(sagaInstance::setStateName);

    sagaInstance.setSerializedSagaData(SagaDataSerde.serializeSagaData(actions.getUpdatedSagaData().orElse(sagaData)));

    if (actions.isEndState()) {
      performEndStateActions(sagaId, sagaInstance);
    }

    sagaInstanceRepository.update(sagaInstance);
  }


  private Boolean isReplyForThisSagaType(Message message) {
    return message.getHeader(SagaReplyHeaders.REPLY_SAGA_TYPE).map(x -> x.equals(getSagaType())).orElse(false);
  }


}
