package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.consumer.common.DecoratedMessageHandlerFactory;
import io.eventuate.tram.consumer.common.MessageConsumerImpl;
import io.eventuate.tram.inmemory.EventuateTransactionSynchronizationManager;
import io.eventuate.tram.inmemory.InMemoryMessageConsumer;
import io.eventuate.tram.inmemory.InMemoryMessageProducer;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.MessageProducer;
import io.eventuate.tram.messaging.producer.common.MessageProducerImpl;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.SagaCommandProducer;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepository;
import io.eventuate.tram.sagas.orchestration.SagaManagerFactory;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NestedSagaTest {
    private SagaInstanceFactory sagaInstanceFactory;
    private OuterSaga outerSaga = new OuterSaga();
    private InnerSaga innerSaga;

    private MessageConsumerImpl messageConsumer;
    private CommandProducerImpl commandProducer;
    private CommandReplyProducer commandReplyProducer;

    @Before
    public void setup() {

        // TODO - copy/paste

        InMemoryMessageConsumer inMemoryMessageConsumer = new InMemoryMessageConsumer();
        EventuateTransactionSynchronizationManager eventuateTransactionSynchronizationManager = mock(EventuateTransactionSynchronizationManager.class);
        when(eventuateTransactionSynchronizationManager.isTransactionActive()).thenReturn(false);

        ChannelMapping channelMapping = new DefaultChannelMapping.DefaultChannelMappingBuilder().build();
        MessageProducer messageProducer = new MessageProducerImpl(new MessageInterceptor[0], channelMapping,
                new InMemoryMessageProducer(inMemoryMessageConsumer, eventuateTransactionSynchronizationManager));

        messageConsumer = new MessageConsumerImpl(channelMapping, inMemoryMessageConsumer, new DecoratedMessageHandlerFactory(Collections.emptyList()));

        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        commandProducer = new CommandProducerImpl(messageProducer, commandNameMapping);

        SagaCommandProducer sagaCommandProducer = new SagaCommandProducer(commandProducer);

        SagaLockManager sagaLockManager = mock(SagaLockManager.class);
        when(sagaLockManager.unlock(any(), any())).thenReturn(Optional.empty());

        SagaInstanceRepository sagaInstanceRepository = new InMemorySagaInstanceRepository();

        commandReplyProducer = new CommandReplyProducer(messageProducer);

        innerSaga = new InnerSaga(commandReplyProducer);

        SagaManagerFactory sagaManagerFactory = new SagaManagerFactory(sagaInstanceRepository, commandProducer, messageConsumer, sagaLockManager, sagaCommandProducer);
        sagaInstanceFactory = new SagaInstanceFactory(sagaManagerFactory, Arrays.asList(outerSaga, innerSaga));

        ParticipantCommandHandlers participantCommandHandlers = new ParticipantCommandHandlers(sagaInstanceFactory, innerSaga);
        CommandHandlers commandHandlers = participantCommandHandlers.commandHandlerDefinitions();
        SagaCommandDispatcherFactory commandDispatcherFactory = new SagaCommandDispatcherFactory(messageConsumer, sagaLockManager, commandNameMapping, commandReplyProducer);
        CommandDispatcher commandDispatcher = commandDispatcherFactory.make("subscriberId", commandHandlers);
        commandDispatcher.initialize();
    }

    @Test
    public void shouldInvokeNestedSaga() throws InterruptedException {
        OuterSagaData data = new OuterSagaData();
        sagaInstanceFactory.create(outerSaga, data);
        TimeUnit.SECONDS.sleep(5);
    }
}
