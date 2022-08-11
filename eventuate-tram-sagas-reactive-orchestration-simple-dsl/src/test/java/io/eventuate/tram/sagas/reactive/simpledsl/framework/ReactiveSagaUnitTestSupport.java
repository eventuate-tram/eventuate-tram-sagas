package io.eventuate.tram.sagas.reactive.simpledsl.framework;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.*;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.common.MessageInterceptor;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.commands.producer.ReactiveCommandProducerImpl;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;
import io.eventuate.tram.sagas.orchestration.SagaDataSerde;
import io.eventuate.tram.sagas.orchestration.SagaInstance;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSaga;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaCommandProducer;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceRepository;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaManagerImpl;
import org.junit.Assert;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertTrue;

/**
 * Provides a DSL for writing unit tests for saga orchestrators
 */
public class ReactiveSagaUnitTestSupport<T> {

  private ReactiveSagaManagerImpl sagaManager;
  private Command expectedCommand;
  private Command expectedNotification;

  private List<MessageWithDestination> sentCommands = new ArrayList<>();
  private MessageWithDestination sentCommand;
  private Optional<Exception> createException = Optional.empty();


  public static final String SAGA_ID = "1";

  private int counter = 2;
  private boolean expectingReply;

  private String genId() {
    return Integer.toString(counter++);
  }

  private volatile SagaInstance sagaInstance;

  public static ReactiveSagaUnitTestSupport<?> given() {
    return new ReactiveSagaUnitTestSupport<>();
  }

  public <T> ReactiveSagaUnitTestSupport<T> saga(ReactiveSaga<T> saga, T sagaData) {
    ReactiveSagaInstanceRepository sagaInstanceRepository = new ReactiveSagaInstanceRepository() {


      @Override
      public Mono<Void> save(SagaInstance sagaInstance) {
        sagaInstance.setId(SAGA_ID);
        ReactiveSagaUnitTestSupport.this.sagaInstance = sagaInstance;
        return Mono.empty();
      }

      @Override
      public Mono<SagaInstance> find(String sagaType, String sagaId) {
        return Mono.just(sagaInstance);
      }

      @Override
      public Mono<Void> update(SagaInstance sagaInstance) {
        ReactiveSagaUnitTestSupport.this.sagaInstance = sagaInstance;
        return Mono.empty();
      }

    };

    CommandNameMapping commandNameMapping = new DefaultCommandNameMapping();

    ReactiveCommandProducerImpl commandProducer = new ReactiveCommandProducerImpl(new ReactiveMessageProducer(new MessageInterceptor[0], new DefaultChannelMapping(emptyMap()), (message) -> {
      String id = genId();
      message.setHeader(Message.ID, id);
      MessageWithDestination mwd = new MessageWithDestination(message.getRequiredHeader(Message.DESTINATION), message);
      System.out.println("mwd=" + mwd);
      sentCommands.add(mwd);
      return Mono.just(message);
    }), commandNameMapping);


    ReactiveSagaCommandProducer sagaCommandProducer = new ReactiveSagaCommandProducer(commandProducer);

    ReactiveMessageConsumer messageConsumer = null;
    ReactiveSagaLockManager sagaLockManager = null;

    sagaManager = new ReactiveSagaManagerImpl<>(saga, sagaInstanceRepository, commandProducer, messageConsumer,
            sagaLockManager, sagaCommandProducer);


    try {
      sagaManager.create(sagaData).block(Duration.ofSeconds(1));
      System.out.println("Created saga");
    } catch (Exception e) {
      createException = Optional.of(e);
    }
    return (ReactiveSagaUnitTestSupport<T>) this;
  }

  public ReactiveSagaUnitTestSupport<T> expect() {
    createException.ifPresent(e -> {
      throw new RuntimeException("Saga creation failed: ", e);
    });
    return this;
  }

  public ReactiveSagaUnitTestSupport<T> command(Command command) {
    expectedCommand = command;
    expectedNotification = null;
    return this;
  }

  public ReactiveSagaUnitTestSupport<T> notification(Command command) {
    expectedCommand = null;
    expectedNotification = command;
    return this;
  }

  public ReactiveSagaUnitTestSupport<T> to(String commandChannel) {
    Assert.assertEquals("Expected one command", 1, sentCommands.size());
    sentCommand = sentCommands.get(0);
    Assert.assertEquals(commandChannel, sentCommand.getDestination());
    Message sentMessage = sentCommand.getMessage();
    if (expectedCommand != null) {
      Assert.assertEquals(expectedCommand.getClass().getName(), sentMessage.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE));
      Assert.assertNotNull(sentMessage.getRequiredHeader(CommandMessageHeaders.REPLY_TO));
      this.expectingReply = true;
    } else {
      Assert.assertEquals(expectedNotification.getClass().getName(), sentMessage.getRequiredHeader(CommandMessageHeaders.COMMAND_TYPE));
      Assert.assertNull(sentMessage.getHeader(CommandMessageHeaders.REPLY_TO).orElse(null));
      this.expectingReply = false;
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

  public ReactiveSagaUnitTestSupport<T> withExtraHeaders(Map<String, String> expectedExtraHeaders) {
    Map<String, String> actualHeaders = sentCommand.getMessage().getHeaders();
    if (!actualHeaders.entrySet().containsAll(expectedExtraHeaders.entrySet()))
      Assert.fail(String.format("Expected headers %s to contain %s", actualHeaders, expectedExtraHeaders));
    return this;
  }

  public ReactiveSagaUnitTestSupport<T> andGiven() {
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


  public ReactiveSagaUnitTestSupport<T> successReply() {
    Success reply = new Success();
    return successReply(reply);
  }

  public ReactiveSagaUnitTestSupport<T> successReply(Object reply) {
    CommandReplyOutcome outcome = CommandReplyOutcome.SUCCESS;
    sendReply(reply, outcome);
    return this;
  }

  public ReactiveSagaUnitTestSupport<T> failureReply() {
    Failure reply = new Failure();
    return failureReply(reply);
  }

  public ReactiveSagaUnitTestSupport<T> failureReply(Object reply) {
    CommandReplyOutcome outcome = CommandReplyOutcome.FAILURE;
    sendReply(reply, outcome);
    return this;
  }

  private void sendReply(Object reply, CommandReplyOutcome outcome) {
    assertTrue("Sending reply but a command was not sent", expectingReply);
    Message message = MessageBuilder
            .withPayload(JSonMapper.toJson(reply))
            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, outcome.name())
            .withHeader(ReplyMessageHeaders.REPLY_TYPE, ((Object) reply).getClass().getName())
            .withExtraHeaders("", correlationHeaders(sentCommand.getMessage().getHeaders()))
            .build();
    String id = genId();
    message.getHeaders().put(Message.ID, id);
    Mono.from(sagaManager.handleMessage(message)).block(Duration.ofSeconds(1));
  }

  public ReactiveSagaUnitTestSupport<T> expectCompletedSuccessfully() {
    assertNoCommands();
    assertTrue("Expected saga to have finished", sagaInstance.isEndState());
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

  public ReactiveSagaUnitTestSupport<T> expectRolledBack() {
    assertNoCommands();
    assertTrue("Expected saga to have finished", sagaInstance.isEndState());
    assertTrue("Expected saga to have rolled back", sagaInstance.isCompensating());
    return this;
  }

  public void expectException(Exception expectedCreateException) {
    Assert.assertEquals(expectedCreateException, createException.get());
  }

  public ReactiveSagaUnitTestSupport<T> assertSagaData(Consumer<T> sagaDataConsumer) {
    sagaDataConsumer.accept(SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData()));
    return this;
  }

  public MultipleCommandsExpected multiple() {
    return new MultipleCommandsExpected(this);
  }

}
