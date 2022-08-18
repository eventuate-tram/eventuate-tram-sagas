package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.inmemory.InMemoryMessaging;
import io.eventuate.tram.sagas.orchestration.SagaInstance;
import io.eventuate.util.test.async.Eventually;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class NestedSagaTest {

    protected final InMemoryMessaging inMemoryMessaging = InMemoryMessaging.make();
    protected final InMemoryCommandProducer inMemoryCommandProducer = InMemoryCommandProducer.make(inMemoryMessaging);
    private InMemorySagaOrchestrationAndParticipants inMemorySagaOrchestrationAndParticipants;

    private final OuterSaga outerSaga = new OuterSaga();
    private InnerSaga innerSaga;

    @Before
    public void setup() {
        innerSaga = new InnerSaga(inMemoryCommandProducer.commandReplyProducer);
        inMemorySagaOrchestrationAndParticipants = InMemorySagaOrchestrationAndParticipants.make(
                inMemoryMessaging, inMemoryCommandProducer, Arrays.asList(outerSaga, innerSaga),
                inMemorySagaOrchestrationAndParticipants -> {
                    ParticipantCommandHandlers participantCommandHandlers = new ParticipantCommandHandlers(inMemorySagaOrchestrationAndParticipants.sagaInstanceFactory, innerSaga);
                    return participantCommandHandlers.commandHandlerDefinitions();
                });
    }

    @Test
    public void shouldInvokeNestedSaga() {
        OuterSagaData data = new OuterSagaData();
        SagaInstance si = inMemorySagaOrchestrationAndParticipants.sagaInstanceFactory.create(outerSaga, data);
        Eventually.eventually(() -> assertTrue(inMemorySagaOrchestrationAndParticipants.sagaInstanceRepository.find(si.getSagaType(), si.getId()).isEndState()));
    }
}
