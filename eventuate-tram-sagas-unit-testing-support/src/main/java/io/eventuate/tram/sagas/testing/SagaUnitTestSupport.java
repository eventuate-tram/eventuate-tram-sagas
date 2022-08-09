package io.eventuate.tram.sagas.testing;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.*;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.common.SagaLockManager;
import io.eventuate.tram.sagas.orchestration.*;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Provides a DSL for writing unit tests for saga orchestrators
 */
public class SagaUnitTestSupport<T> {

  private SagaManagerImpl sagaManager;
  private Command expectedCommand;
  private Command expectedNotification;

  private List<MessageWithDestination> sentCommands = new ArrayList<>();
  private MessageWithDestination sentCommand;
  private Optional<Exception> createException = Optional.empty();


  public static final String SAGA_ID = "1";

  private int counter = 2;

  private String genId() {
    return Integer.toString(counter++);
  }

  private SagaInstance sagaInstance;

  public static SagaUnitTestSupport<?> given() {
    return new SagaUnitTestSupport<>();
  }

  public <T> SagaUnitTestSupport<T> saga(Saga<T> saga, T sagaData) {
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

    CommandNameMapping commandNameMapping = new DefaultCommandNameMapping();

    CommandProducerImpl commandProducer = new CommandProducerImpl((destination, message) -> {
      String id = genId();
      message.setHeader(Message.ID, id);
      sentCommands.add(new MessageWithDestination(destination, message));
    }, commandNameMapping);

    SagaCommandProducer sagaCommandProducer = new SagaCommandProducer(commandProducer);

    MessageConsumer messageConsumer = null;
    SagaLockManager sagaLockManager = null;

    sagaManager = new SagaManagerImpl<>(saga, sagaInstanceRepository, commandProducer, messageConsumer,
            sagaLockManager, sagaCommandProducer);


    try {
      sagaManager.create(sagaData);
    } catch (Exception e) {
      createException = Optional.of(e);
    }
    return (SagaUnitTestSupport<T>) this;
  }

  public SagaUnitTestSupport<T> expect() {
    createException.ifPresent(e -> {
      throw new RuntimeException("Saga creation failed: ", e);
    });
    return this;
  }

  public SagaUnitTestSupport<T> command(Command command) {
    expectedCommand = command;
    expectedNotification = null;
    return this;
  }

  public SagaUnitTestSupport<T> notification(Command command) {
    expectedCommand = null;
    expectedNotification = command;
    return this;
  }

  public SagaUnitTestSupport<T> to(String commandChannel) {
    Assert.assertEquals("Expected one command", 1, sentCommands.size());
    sentCommand = sentCommands.get(0);
    Assert.assertEquals(commandChannel, sentCommand.getDestination());
    Message sentMessage = sentCommand.getMessage();
    if (expectedCommand != null) {
      Assert.assertEquals(expectedCommand.getClass().getName(), sentMessage.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE));
      Assert.assertNotNull(sentMessage.getRequiredHeader(CommandMessageHeaders.REPLY_TO));
    } else {
      Assert.assertEquals(expectedNotification.getClass().getName(), sentMessage.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE));
      Assert.assertNull(sentMessage.getHeader(CommandMessageHeaders.REPLY_TO).orElse(null));

    }
    sentCommands.clear();
    return this;
  }

  void verifySent(List<CommandWithDestinationAndType> commandsAndNotifications) {
    sentCommand = null;
    for (CommandWithDestinationAndType corn : commandsAndNotifications) {
      MessageWithDestination sentMessage = sentCommands.stream()
              .filter(sm -> corn.getCommandWithDestination().getCommand().getClass().getName().equals(sm.getMessage().getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE))
                          && corn.getCommandWithDestination().getDestinationChannel().equals(sm.getDestination()))
              .findAny()
              .orElseThrow(() -> new AssertionError(String.format("Did not find expected command %s in %s", corn, sentCommands)));

      if (corn.isNotification())
        Assert.assertNull(sentMessage.getMessage().getHeader(CommandMessageHeaders.REPLY_TO).orElse(null));
      else {
        Assert.assertNotNull(sentMessage.getMessage().getRequiredHeader(CommandMessageHeaders.REPLY_TO));
        if (sentCommand != null)
          Assert.fail(String.format("There can only be one command in %s", sentCommands));
        sentCommand = sentMessage;
      }
    }
    if (commandsAndNotifications.size() != sentCommands.size())
      Assert.fail(String.format("Expected these commands %s but there are extra %s", commandsAndNotifications, sentCommands));
    sentCommands.clear();
  }

  public SagaUnitTestSupport<T> withExtraHeaders(Map<String, String> expectedExtraHeaders) {
    Map<String, String> actualHeaders = sentCommand.getMessage().getHeaders();
    if (!actualHeaders.entrySet().containsAll(expectedExtraHeaders.entrySet()))
      Assert.fail(String.format("Expected headers %s to contain %s", actualHeaders, expectedExtraHeaders));
    return this;
  }

  public SagaUnitTestSupport<T> andGiven() {
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


  public SagaUnitTestSupport<T> successReply() {
    Success reply = new Success();
    return successReply(reply);
  }

  public SagaUnitTestSupport<T> successReply(Object reply) {
    CommandReplyOutcome outcome = CommandReplyOutcome.SUCCESS;
    sendReply(reply, outcome);
    return this;
  }

  public SagaUnitTestSupport<T> failureReply() {
    Failure reply = new Failure();
    return failureReply(reply);
  }

  public SagaUnitTestSupport<T> failureReply(Object reply) {
    CommandReplyOutcome outcome = CommandReplyOutcome.FAILURE;
    sendReply(reply, outcome);
    return this;
  }

  private void sendReply(Object reply, CommandReplyOutcome outcome) {
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

  public SagaUnitTestSupport<T> expectCompletedSuccessfully() {
    assertNoCommands();
    Assert.assertTrue("Expected saga to have finished", sagaInstance.isEndState());
    Assert.assertFalse("Expected saga to have finished successfully", sagaInstance.isCompensating());
    return this;
  }

  private void assertNoCommands() {
    switch (sentCommands.size()) {
      case 0:
        break;
      case 1:
        MessageWithDestination mwd = sentCommands.get(0);
        Assert.fail(String.format("Expected saga to have finished but found a command of %s sent to %s: %s", mwd.getMessage().getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE), mwd.getDestination(), mwd.getMessage()));
        break;
      default:
        Assert.assertEquals(emptyList(), sentCommands);
    }
  }

  public SagaUnitTestSupport<T> expectRolledBack() {
    assertNoCommands();
    Assert.assertTrue("Expected saga to have finished", sagaInstance.isEndState());
    Assert.assertTrue("Expected saga to have rolled back", sagaInstance.isCompensating());
    return this;
  }

  public void expectException(Exception expectedCreateException) {
    Assert.assertEquals(expectedCreateException, createException.get());
  }

  public SagaUnitTestSupport<T> assertSagaData(Consumer<T> sagaDataConsumer) {
    sagaDataConsumer.accept(SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData()));
    return this;
  }

  public MultipleCommandsExpected multiple() {
    return new MultipleCommandsExpected(this);
  }

}
