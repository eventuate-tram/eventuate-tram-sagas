package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.commands.consumer.CommandDispatcher;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.inmemory.InMemoryMessaging;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.*;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class InMemorySagaOrchestrationAndParticipants {

    public final SagaInstanceFactory sagaInstanceFactory;
    public final SagaCommandDispatcherFactory sagaCommandDispatcherFactory;
    public final SagaInstanceRepository sagaInstanceRepository;

    public InMemorySagaOrchestrationAndParticipants(SagaInstanceFactory sagaInstanceFactory, SagaCommandDispatcherFactory sagaCommandDispatcherFactory, SagaInstanceRepository sagaInstanceRepository) {

        this.sagaInstanceFactory = sagaInstanceFactory;
        this.sagaCommandDispatcherFactory = sagaCommandDispatcherFactory;
        this.sagaInstanceRepository = sagaInstanceRepository;
    }

    public static InMemorySagaOrchestrationAndParticipants make(InMemoryMessaging inMemoryMessagingFactory,
                                                                InMemoryCommandProducer inMemoryCommandProducer,
                                                                List<Saga<?>> sagas,
                                                                Function<InMemorySagaOrchestrationAndParticipants, CommandHandlers> commandHandlersSupplier) {
        SagaCommandProducer sagaCommandProducer = new SagaCommandProducer(inMemoryCommandProducer.commandProducer);

        SagaLockManager sagaLockManager = new SagaLockManager() {

            @Override
            public boolean claimLock(String sagaType, String sagaId, String target) {
                return false;
            }

            @Override
            public void stashMessage(String sagaType, String sagaId, String target, Message message) {

            }

            @Override
            public Optional<Message> unlock(String sagaId, String target) {
                return Optional.empty();
            }
        };

        SagaInstanceRepository sagaInstanceRepository = new InMemorySagaInstanceRepository();


        SagaManagerFactory sagaManagerFactory = new SagaManagerFactory(sagaInstanceRepository, inMemoryCommandProducer.commandProducer, inMemoryMessagingFactory.messageConsumer, sagaLockManager, sagaCommandProducer);
        SagaInstanceFactory sagaInstanceFactory = new SagaInstanceFactory(sagaManagerFactory, sagas);

        SagaCommandDispatcherFactory sagaCommandDispatcherFactory = new SagaCommandDispatcherFactory(inMemoryMessagingFactory.messageConsumer, sagaLockManager, inMemoryCommandProducer.commandNameMapping, inMemoryCommandProducer.commandReplyProducer);

        InMemorySagaOrchestrationAndParticipants inMemorySagaOrchestrationAndParticipants = new InMemorySagaOrchestrationAndParticipants(sagaInstanceFactory, sagaCommandDispatcherFactory, sagaInstanceRepository);

        inMemorySagaOrchestrationAndParticipants.initialize(commandHandlersSupplier);

        return inMemorySagaOrchestrationAndParticipants;
    }

    private void initialize(Function<InMemorySagaOrchestrationAndParticipants, CommandHandlers> commandHandlersSupplier) {
        CommandDispatcher commandDispatcher = sagaCommandDispatcherFactory.make("subscriberId", commandHandlersSupplier.apply(this));
        commandDispatcher.initialize();
    }
}
