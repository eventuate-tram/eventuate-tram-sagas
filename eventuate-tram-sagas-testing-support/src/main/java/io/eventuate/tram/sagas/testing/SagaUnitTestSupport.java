package io.eventuate.tram.sagas.testing;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.*;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.orchestration.*;
import io.eventuate.tram.sagas.common.SagaLockManager;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.junit.Assert.*;

/**
 * Provides a DSL for writing unit tests for saga orchestrators
 */
public class SagaUnitTestSupport {

  private SagaManagerImpl sagaManager;
  private Command expectedCommand;

  private List<MessageWithDestination> sentCommands = new ArrayList<>();
  private MessageWithDestination sentCommand;
  private Optional<Exception> createException = Optional.empty();

  public static SagaUnitTestSupport given() {
    return new SagaUnitTestSupport();
  }

  public static final String SAGA_ID = "1";
  
  private int counter = 2;
  
  private String genId() {
    return Integer.toString(counter++);  
  }

  private SagaInstance sagaInstance;

  public <T> SagaUnitTestSupport saga(Saga<T> saga, T sagaData) {
    SagaInstanceRepository sagaInstanceRepository = new SagaInstanceRepository() {


      @Override
      public void save(SagaInstance sagaInstance) {
        sagaInstance.setId(SAGA_ID);
        SagaUnitTestSupport.this.sagaInstance = sagaInstance;
      }

      @Override
      public SagaInstance find(String sagaType, String sagaId) {
        return sagaInstance;
      }

      @Override
      public void update(SagaInstance sagaInstance) {
        SagaUnitTestSupport.this.sagaInstance = sagaInstance;
      }

    };

    CommandProducerImpl commandProducer = new CommandProducerImpl((destination, message) -> {
      String id = genId();
      message.getHeaders().put(Message.ID, id);
      sentCommands.add(new MessageWithDestination(destination, message));
    }, new DefaultChannelMapping(Collections.emptyMap()));

    SagaCommandProducer sagaCommandProducer = new SagaCommandProducer(commandProducer);

    MessageConsumer messageConsumer = null;
    SagaLockManager sagaLockManager = null;

    sagaManager = new SagaManagerImpl<>(saga, sagaInstanceRepository, commandProducer, messageConsumer, new DefaultChannelMapping(Collections.emptyMap()),
            sagaLockManager, sagaCommandProducer);


    try {
      sagaManager.create(sagaData);
    } catch (Exception e) {
      createException = Optional.of(e);
    }
    return this;
  }

  public SagaUnitTestSupport expect() {
    assertFalse(createException.isPresent());
    return this;
  }

  public SagaUnitTestSupport command(Command command) {
    expectedCommand = command;
    return this;
  }

  public SagaUnitTestSupport to(String commandChannel) {
    assertEquals("Expected a command", 1, sentCommands.size());
    sentCommand = sentCommands.get(0);
    assertEquals(commandChannel, sentCommand.getDestination());
    assertEquals(expectedCommand.getClass().getName(), sentCommand.getMessage().getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE));
    // TODO 
    sentCommands.clear();
    return this;
  }

  public SagaUnitTestSupport andGiven() {
    return this;
  }

  // copy
  private Map<String, String> correlationHeaders(Map<String, String> headers) {
    Map<String, String> m = headers.entrySet()
            .stream()
            .filter(e -> e.getKey().startsWith(CommandMessageHeaders.COMMAND_HEADER_PREFIX))
            .collect(Collectors.toMap(e -> CommandMessageHeaders.inReply(e.getKey()),
                    Map.Entry::getValue));
    m.put(ReplyMessageHeaders.IN_REPLY_TO, headers.get(Message.ID));
    return m;
  }


  public SagaUnitTestSupport successReply() {
    Success reply = new Success();
    CommandReplyOutcome outcome = CommandReplyOutcome.SUCCESS;
    sendReply(reply, outcome);
    return this;
  }

  public SagaUnitTestSupport failureReply() {
    Failure reply = new Failure();
    CommandReplyOutcome outcome = CommandReplyOutcome.FAILURE;
    sendReply(reply, outcome);
    return this;
  }

  private void sendReply(Outcome reply, CommandReplyOutcome outcome) {
    Message message = MessageBuilder
            .withPayload(JSonMapper.toJson(reply))
            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, outcome.name())
            .withHeader(ReplyMessageHeaders.REPLY_TYPE, ((Object) reply).getClass().getName())
            .withExtraHeaders("", correlationHeaders(sentCommand.getMessage().getHeaders()))
            .build();
    String id = genId();
    message.getHeaders().put(Message.ID, id);
    sagaManager.handleMessage(message);
  }

  public SagaUnitTestSupport expectCompletedSuccessfully() {
    assertNoCommands();
    assertTrue(sagaInstance.isEndState());
    assertFalse(sagaInstance.isCompensating());
    return this;
  }

  private void assertNoCommands() {
    assertEquals(emptyList(), sentCommands);
  }

  public SagaUnitTestSupport expectRolledBack() {
    assertNoCommands();
    assertTrue(sagaInstance.isEndState());
    assertTrue(sagaInstance.isCompensating());
    return this;
  }

  public void expectException(Exception expectedCreateException) {
    assertEquals(expectedCreateException, createException.get());
  }
}
