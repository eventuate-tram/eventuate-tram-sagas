package io.eventuate.tram.sagas.orchestration;

import io.eventuate.Int128;
import io.eventuate.tram.commands.common.ChannelMapping;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.consumer.MessageHandler;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.common.SagaReplyHeaders;
import io.eventuate.tram.sagas.participant.SagaLockManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class SagaManagerImplTest {


  @Mock
  private SagaManagerImpl<TestSagaData> sm;

  @Mock
  private SagaInstanceRepository sagaInstanceRepository;

  @Mock
  private CommandProducer commandProducer;

  @Mock
  private MessageConsumer messageConsumer;

  @Mock
  private ChannelMapping channelMapping;

  @Mock
  private SagaLockManager sagaLockManager;

  @Mock
  private SagaCommandProducer sagaCommandProducer;

  private TestSagaData initialSagaData;
  private TestSagaData sagaDataUpdatedByStartingHandler;
  private TestSagaData sagaDataUpdatedByReplyHandler;

  @Mock
  private TestSaga testSaga;

  @Mock
  private SagaDefinition<TestSagaData> sagaDefinition;


  @Mock
  private RawSagaStateMachineAction replyHandler;

  private String testResource = "SomeResource";
  private String sagaType = "MySagaType";
  private String sagaId = "MySagaId";
  private String sagaReplyChannel = sagaType + "-reply";

  private String participantChannel1 = "myChannel";
  private String participantChannel2 = "myChannel2";

  private TestCommand command1 = new TestCommand();
  private TestCommand command2 = new TestCommand();

  CommandWithDestination commandForParticipant1 = new CommandWithDestination(participantChannel1, testResource,
          SagaManagerImplTest.this.command1);

  CommandWithDestination commandForParticipant2 = new CommandWithDestination(participantChannel2, testResource,
          SagaManagerImplTest.this.command2);

  private SagaInstance sagaInstance;

  private Int128 requestId1 = new Int128(0, 1);
  private Int128 requestId2 = new Int128(0, 2);

  private MessageHandler sagaMessageHandler;

  Message replyFromParticipant1 = MessageBuilder.withPayload("{}")
          .withHeader(SagaReplyHeaders.REPLY_SAGA_TYPE, sagaType)
          .withHeader(SagaReplyHeaders.REPLY_SAGA_ID, sagaId)
          .build();

  @Before
  public void setUp() {

    MockitoAnnotations.initMocks(this);

    sm = new SagaManagerImpl<>(testSaga, sagaInstanceRepository,
            commandProducer, messageConsumer, channelMapping,
            sagaLockManager, sagaCommandProducer);

    initialSagaData = new TestSagaData("initialSagaData");
    sagaDataUpdatedByStartingHandler = new TestSagaData("sagaDataUpdatedByStartingHandler");
    sagaDataUpdatedByReplyHandler = new TestSagaData("sagaDataUpdatedByStartingHandlersagaDataUpdatedByReplyHandler");

    when(testSaga.getSagaType()).thenReturn(sagaType);
    when(testSaga.getSagaDefinition()).thenReturn(sagaDefinition);

  }

  @Test
  public void shouldExecuteSaga() {

    when(channelMapping.transform(sagaReplyChannel)).thenReturn(sagaReplyChannel);

    initializeSagaManager();

    when(sagaDefinition.invokeStartingHandler(initialSagaData)).thenReturn(makeFirstSagaActions());

    when(sagaCommandProducer.sendCommands(anyString(), anyString(), anyList(), anyString()))
            .thenReturn(requestId1.asString());

    assignSagaIdWhenSaved();

    sagaInstance = sm.create(initialSagaData);

    SagaInstance expectedSagaInstanceAfterFirstStep = new SagaInstance(sagaType, sagaId, "state-A", requestId1.asString(), SagaDataSerde
            .serializeSagaData(sagaDataUpdatedByStartingHandler), Collections.emptySet());


    assertSagaInstanceEquals(expectedSagaInstanceAfterFirstStep, sagaInstance);

    verify(sagaCommandProducer).sendCommands(sagaType, sagaId, Collections.singletonList(commandForParticipant1),
            sagaReplyChannel);


    SagaInstance savedSagaInstance = verifySagaInstanceSaved();

    verify(sagaInstanceRepository).update(sagaInstance);

    // Verify what is being persisted

    assertSagaInstanceEquals(expectedSagaInstanceAfterFirstStep, sagaInstance);

    verifyNoMoreInteractions(sagaInstanceRepository, sagaCommandProducer);
    reset(sagaInstanceRepository, sagaCommandProducer);

    // Handle a reply

    when(sagaInstanceRepository.find(sagaType, sagaId))
            .thenReturn(sagaInstance);

    when(sagaDefinition.handleReply(anyString(), any(TestSagaData.class), any(Message.class)))
            .thenReturn(makeSecondSagaActions());

    when(sagaCommandProducer.sendCommands(anyString(), anyString(), anyList(), anyString())).thenReturn
            (requestId2.asString());


    sagaMessageHandler.accept(replyFromParticipant1);

    verify(sagaInstanceRepository).find(sagaType, sagaId);

    verify(sagaCommandProducer).sendCommands(sagaType, sagaId, Collections.singletonList(commandForParticipant2), sagaReplyChannel);

    verify(sagaInstanceRepository).update(sagaInstance);

    SagaInstance expectedSagaInstanceAfterSecondStep = new SagaInstance(sagaType, sagaId, "state-B",
            requestId2.asString(),
            SagaDataSerde.serializeSagaData(sagaDataUpdatedByReplyHandler), Collections.emptySet());

    assertSagaInstanceEquals(expectedSagaInstanceAfterSecondStep, sagaInstance);

    verifyNoMoreInteractions(sagaInstanceRepository, sagaCommandProducer);
    reset(sagaInstanceRepository, sagaCommandProducer);

  }

  private void assertSagaInstanceEquals(SagaInstance expectedSagaInstance, SagaInstance sagaInstance) {
    assertEquals(expectedSagaInstance.getSagaType(), sagaInstance.getSagaType());
    assertEquals(expectedSagaInstance.getId(), sagaInstance.getId());
    assertEquals(expectedSagaInstance.getStateName(), sagaInstance.getStateName());
    assertEquals(expectedSagaInstance.getLastRequestId(), sagaInstance.getLastRequestId());
    assertEquals(expectedSagaInstance.getSerializedSagaData().getSagaDataType(),
            sagaInstance.getSerializedSagaData().getSagaDataType());
    assertEquals(expectedSagaInstance.getSerializedSagaData().getSagaDataJSON(), sagaInstance
            .getSerializedSagaData().getSagaDataJSON());
  }

  private SagaActions<TestSagaData> makeFirstSagaActions() {
    return SagaActions.<TestSagaData>builder()
            .withUpdatedSagaData(sagaDataUpdatedByStartingHandler)
            .withCommand(commandForParticipant1).withUpdatedState("state-A").build();
  }

  private SagaActions<TestSagaData> makeSecondSagaActions() {
    return SagaActions.<TestSagaData>builder()
            .withCommand(commandForParticipant2)
            .withUpdatedState("state-B")
            .withUpdatedSagaData(sagaDataUpdatedByReplyHandler)
            .withIsEndState(true)
            .build();
  }

  private SagaInstance verifySagaInstanceUpdated() {
    ArgumentCaptor<SagaInstance> updateArg = ArgumentCaptor.forClass(SagaInstance.class);
    verify(sagaInstanceRepository).update(updateArg.capture());
    return updateArg.getValue();
  }

  private SagaInstance verifySagaInstanceSaved() {
    ArgumentCaptor<SagaInstance> saveArg = ArgumentCaptor.forClass(SagaInstance.class);
    verify(sagaInstanceRepository).save(saveArg.capture());
    return saveArg.getValue();
  }

  private void assignSagaIdWhenSaved() {
    doAnswer(invocation -> {
      SagaInstance sagaInstance = invocation.getArgumentAt(0, SagaInstance.class);
      sagaInstance.setId(sagaId);
      return null;
    }).when(sagaInstanceRepository).save(any(SagaInstance.class));
  }

  private void initializeSagaManager() {
    sm.subscribeToReplyChannel();

    ArgumentCaptor<MessageHandler> messageHandlerArgumentCaptor = ArgumentCaptor.forClass(MessageHandler.class);
    verify(messageConsumer).subscribe(anyString(), Mockito.eq(Collections.singleton(sagaReplyChannel)),
            messageHandlerArgumentCaptor.capture());

    sagaMessageHandler = messageHandlerArgumentCaptor.getValue();
  }


}