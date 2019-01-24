package io.eventuate.tram.sagas.testing;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.javaclient.spring.jdbc.IdGeneratorImpl;
import io.eventuate.tram.commands.common.*;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.events.common.DomainEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.orchestration.*;
import io.eventuate.tram.sagas.participant.SagaLockManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Provides a DSL for writing unit tests for saga orchestrators
 */
public class SagaUnitTestSupport {

  private SagaManagerImpl sagaManager;
  private Command expectedCommand;

  private List<MessageWithDestination> sentCommands = new ArrayList<>();
  private MessageWithDestination sentCommand;

  public static SagaUnitTestSupport given() {
    return new SagaUnitTestSupport();
  }

  public static final String SAGA_ID = "1";
  
  private int counter = 2;
  
  private String genId() {
    return Integer.toString(counter++);  
  }
  
  public <T> SagaUnitTestSupport saga(Saga<T> saga, T sagaData) {
    SagaInstanceRepository sagaInstanceRepository = new SagaInstanceRepository() {

      private SagaInstance sagaInstance;

      @Override
      public void save(SagaInstance sagaInstance) {
        sagaInstance.setId(SAGA_ID);
        this.sagaInstance = sagaInstance;
      }

      @Override
      public SagaInstance find(String sagaType, String sagaId) {
        return sagaInstance;
      }

      @Override
      public void update(SagaInstance sagaInstance) {
        this.sagaInstance = sagaInstance;
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


    sagaManager.create(sagaData);
    return this;
  }

  public SagaUnitTestSupport expect() {
    return this;
  }

  public SagaUnitTestSupport command(Command command) {
    expectedCommand = command;
    return this;
  }

  public SagaUnitTestSupport to(String commandChannel) {
    assertEquals(1, sentCommands.size());
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
    Message message = replyMessage(reply, outcome);
    String id = genId();
    message.getHeaders().put(Message.ID, id);
    sagaManager.handleMessage(message);
    return this;
  }

  public SagaUnitTestSupport failureReply() {
    Failure reply = new Failure();
    CommandReplyOutcome outcome = CommandReplyOutcome.FAILURE;
    Message message = replyMessage(reply, outcome);
    String id = genId();
    message.getHeaders().put(Message.ID, id);
    sagaManager.handleMessage(message);
    return this;
  }

  private Message replyMessage(Object reply, CommandReplyOutcome outcome) {
    return MessageBuilder
            .withPayload(JSonMapper.toJson(reply))
            .withHeader(ReplyMessageHeaders.REPLY_OUTCOME, outcome.name())
            .withHeader(ReplyMessageHeaders.REPLY_TYPE, reply.getClass().getName())
            .withExtraHeaders("", correlationHeaders(sentCommand.getMessage().getHeaders()))
            .build();
  }

  public SagaUnitTestSupport expectCompletedSuccessfully() {
    assertEquals(emptyList(), sentCommands);
    return this;
  }

  public SagaUnitTestSupport expectRolledBack() {
    assertEquals(emptyList(), sentCommands);
    return this;
  }

}
