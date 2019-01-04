package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;
import io.eventuate.tram.sagas.common.SagaReplyHeaders;
import io.eventuate.tram.sagas.common.SagaUnlockCommand;
import io.eventuate.tram.sagas.participant.SagaLockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singleton;

public class SagaOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(SagaOrchestrator.class);
    private static final String CONSUMER_SUFFIX = "-consumer";
    private static final String REPLY_SUFFIX = "-reply";

    private final SagaRegistrar sagaRegistrar;
    private final ChannelMapping channelMapping;
    private final MessageConsumer messageConsumer;
    private final SagaLockManager sagaLockManager;
    private final CommandProducer commandProducer;
    private final SagaCommandProducer sagaCommandProducer;
    private final SagaInstanceRepository sagaInstanceRepository;

    public SagaOrchestrator(final SagaRegistrar sagaRegistrar,
                            final ChannelMapping channelMapping,
                            final MessageConsumer messageConsumer,
                            final SagaLockManager sagaLockManager,
                            final CommandProducer commandProducer,
                            final SagaCommandProducer sagaCommandProducer,
                            final SagaInstanceRepository sagaInstanceRepository) {
        this.sagaRegistrar = sagaRegistrar;
        this.channelMapping = channelMapping;
        this.messageConsumer = messageConsumer;
        this.sagaLockManager = sagaLockManager;
        this.commandProducer = commandProducer;
        this.sagaCommandProducer = sagaCommandProducer;
        this.sagaInstanceRepository = sagaInstanceRepository;
    }

    public void subscribe(final String sagaType, final MessageHandler messageHandler) {
        messageConsumer.subscribe(sagaType + CONSUMER_SUFFIX, singleton(channelMapping.transform(makeSagaReplyChannel(sagaType))),
                messageHandler);
    }

    public <Data> SagaInstance registerSaga(Saga<Data> saga,
                                            final Data sagaData,
                                            final String resource) {
        final SagaInstance sagaInstance = new SagaInstance(saga.getSagaType(),
                null,
                "????",
                null,
                SagaDataSerde.serializeSagaData(sagaData), new HashSet<>());

        sagaInstanceRepository.save(sagaInstance);

        final String sagaId = sagaInstance.getId();

        Optional.ofNullable(resource).ifPresent(r -> Assert.isTrue(sagaLockManager.claimLock(saga.getSagaType(), sagaId, r), "Cannot claim lock for resource"));

        final SagaActions<Data> actions = saga.getSagaDefinition().getActions(sagaData);

        processActions(sagaData, actions, sagaInstance);

        return sagaInstance;
    }

    private <Data> void processActions(final Data sagaData,
                                       final SagaActions<Data> actions,
                                       final SagaInstance sagaInstance) {

        final String sagaReplyChannel = makeSagaReplyChannel(sagaInstance.getSagaType());
        String lastRequestId = sagaCommandProducer.sendCommands(sagaInstance.getSagaType(), sagaInstance.getId(), actions.getCommands(), sagaReplyChannel);
        sagaInstance.setLastRequestId(lastRequestId);

        actions.getUpdatedState().ifPresent(sagaInstance::setStateName);

        sagaInstance.setSerializedSagaData(SagaDataSerde.serializeSagaData(actions.getUpdatedSagaData().orElse(sagaData)));

        if (actions.isEndState()) {
            performEndStateActions(sagaInstance, sagaData, actions.isCompensating());
        }

        sagaInstanceRepository.update(sagaInstance);
    }

    private <Data> void performEndStateActions(final SagaInstance sagaInstance,
                                               final Data data,
                                               final boolean compensating) {
        for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
            Map<String, String> headers = new HashMap<>();
            headers.put(SagaCommandHeaders.SAGA_ID, sagaInstance.getId());
            headers.put(SagaCommandHeaders.SAGA_TYPE, sagaInstance.getSagaType()); // FTGO SagaCommandHandler failed without this but the OrdersAndCustomersIntegrationTest was fine?!?
            commandProducer.send(dr.getDestination(), dr.getResource(), new SagaUnlockCommand(), makeSagaReplyChannel(sagaInstance.getSagaType()), headers);
        }

        sagaRegistrar.getSagas().stream()
                .filter(saga -> saga.getSagaType().equals(sagaInstance.getSagaType()))
                .findFirst()
                .ifPresent(saga -> {
                    if (compensating) {
                        saga.onSagaRolledBack(sagaInstance.getId(), data);
                    } else {
                        saga.onSagaCompletedSuccessfully(sagaInstance.getId(), data);
                    }
                });

    }


    public void handleMessage(Message message) {
        logger.debug("handle message invoked {}", message);
        if (message.hasHeader(SagaReplyHeaders.REPLY_SAGA_ID)) {
            handleReply(message);
        } else {
            logger.warn("Handle message doesn't know what to do with: {} ", message);
        }
    }


    private void handleReply(final Message message) {

        logger.debug("Handle reply: {}", message);

        final String sagaId = message.getRequiredHeader(SagaReplyHeaders.REPLY_SAGA_ID);
        final String sagaType = message.getRequiredHeader(SagaReplyHeaders.REPLY_SAGA_TYPE);

        final SagaInstance sagaInstance = sagaInstanceRepository.find(sagaType, sagaId);
        final Object sagaData = SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData());


        message.getHeader(SagaReplyHeaders.REPLY_LOCKED).ifPresent(lockedTarget -> {
            String destination = message.getRequiredHeader(CommandMessageHeaders.inReply(CommandMessageHeaders.DESTINATION));
            sagaInstance.addDestinationsAndResources(singleton(new DestinationAndResource(destination, lockedTarget)));
        });

        String currentState = sagaInstance.getStateName();

        logger.info("Current state={}", currentState);

        final SagaActions<Object> actions = sagaRegistrar.getSagas().stream()
                .filter(saga -> saga.getSagaType().equals(sagaInstance.getSagaType()))
                .findFirst()
                .map(Saga::getSagaDefinition)
                .map(definition -> definition.getReplyActions(currentState, sagaData, message))
                .orElseThrow(() -> new RuntimeException("Can't find Saga!"));

        logger.info("Handled reply. Sending commands {}", actions.getCommands());

        processActions(sagaData, actions, sagaInstance);

    }


    private String makeSagaReplyChannel(final String sagaType) {
        return sagaType + REPLY_SUFFIX;
    }

}
