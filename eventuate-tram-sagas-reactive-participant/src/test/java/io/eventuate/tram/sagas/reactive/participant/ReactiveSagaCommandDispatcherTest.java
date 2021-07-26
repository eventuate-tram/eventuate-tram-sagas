package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder;
import io.eventuate.tram.consumer.common.reactive.ReactiveMessageConsumer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandler;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandlers;
import io.eventuate.tram.reactive.messaging.producer.common.ReactiveMessageProducer;
import io.eventuate.tram.sagas.reactive.common.ReactiveSagaLockManager;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class ReactiveSagaCommandDispatcherTest {
  @Test
  public void testHandlerIsInvokedOnlyOnce() {
    Function<String, Message> messageHandlerConsumer = createMessageHandlerConsumer();

    ReactiveSagaCommandDispatcher reactiveSagaCommandDispatcher =
            new ReactiveSagaCommandDispatcher("commandDispatcherId1", createReactiveCommandHandlers(messageHandlerConsumer), mockConsumer(), mockProducer(), mockLockManager());

    Mono.from(reactiveSagaCommandDispatcher.messageHandler(createCommandMessage())).block();

    Mockito.verify(messageHandlerConsumer).apply(Mockito.any());
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
            new ReactiveCommandHandler("channel1", Optional.empty(), TestCommand.class, (message, pathVariables) ->
                    Flux.just("TEST ITERATION VALUE").map(messageHandlerConsumer));

    return new ReactiveCommandHandlers(Collections.singletonList(reactiveCommandHandler));
  }

  private Function<String, Message> createMessageHandlerConsumer() {
    Function<String, Message> messageHandlerConsumer = Mockito.mock(Function.class);

    Mockito.when(messageHandlerConsumer.apply(Mockito.any())).thenReturn(CommandHandlerReplyBuilder.withSuccess());

    return messageHandlerConsumer;
  }

  private ReactiveMessageProducer mockProducer() {
    ReactiveMessageProducer producer =  Mockito.mock(ReactiveMessageProducer.class);

    Mockito.when(producer.send(Mockito.any(), Mockito.any())).thenReturn(Mono.just(CommandHandlerReplyBuilder.withSuccess()));

    return producer;
  }

  private ReactiveSagaLockManager mockLockManager() {
    return Mockito.mock(ReactiveSagaLockManager.class);
  }

  private ReactiveMessageConsumer mockConsumer() {
    return Mockito.mock(ReactiveMessageConsumer.class);
  }

  public static class TestCommand implements Command {}
}
