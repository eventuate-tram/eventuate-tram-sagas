package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandler;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandReplyProducer;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReactiveSagaCommandDispatcherTest {

  @Mock
  private Function<String, Message> messageHandlerConsumer;
  @Mock
  private ReactiveMessageProducer producer;
  @Mock
  private ReactiveSagaLockManager sagaLockManager;
  @Mock
  private ReactiveMessageConsumer messageConsumer;

  @Before
  public void setUp() {
    when(messageHandlerConsumer.apply(any())).thenReturn(CommandHandlerReplyBuilder.withSuccess());
    when(producer.send(any(), any())).thenReturn(Mono.just(CommandHandlerReplyBuilder.withSuccess()));
  }

  @Test
  public void testHandlerIsInvokedOnlyOnce() {

    ReactiveSagaCommandDispatcher reactiveSagaCommandDispatcher =
            new ReactiveSagaCommandDispatcher("commandDispatcherId1", createReactiveCommandHandlers(messageHandlerConsumer), messageConsumer, sagaLockManager,
                    new ReactiveCommandReplyProducer(producer));

    Mono.from(reactiveSagaCommandDispatcher.messageHandler(createCommandMessage())).block();

    verify(messageHandlerConsumer).apply(any());
  }

  private Message createCommandMessage() {
    return MessageBuilder
            .withPayload("{}")
            .withHeader(CommandMessageHeaders.COMMAND_TYPE, TestCommand.class.getName())
            .withHeader(CommandMessageHeaders.REPLY_TO, "channel2")
            .withHeader("ID", "id1")
            .build();
  }

  private ReactiveCommandHandlers createReactiveCommandHandlers(Function<String, Message> messageHandlerConsumer) {
    ReactiveCommandHandler reactiveCommandHandler =
            new ReactiveCommandHandler("channel1", Optional.empty(), TestCommand.class, (args) ->
                    Flux.just("TEST ITERATION VALUE").map(messageHandlerConsumer));

    return new ReactiveCommandHandlers(Collections.singletonList(reactiveCommandHandler));
  }

  public static class TestCommand implements Command {}
}
