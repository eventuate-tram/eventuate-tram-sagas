package io.eventuate.tram.sagas.reactive.participant;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandHandlerParams;
import io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.reactive.commands.consumer.ReactiveCommandHandler;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

public class ReactiveSagaCommandDispatcherTest {
  @Test
  public void testHandlerIsInvokedOnlyOnce() {
    ReactiveSagaCommandDispatcher reactiveSagaCommandDispatcher =
            new ReactiveSagaCommandDispatcher(null, null, null, null, null);

    Function<String, Message> messageHandlerConsumer = createMessageHandlerConsumer();

    Mono.from(reactiveSagaCommandDispatcher.invoke(createReactiveCommandHandler(messageHandlerConsumer), null, createCommandHandlerParams())).block();

    Mockito.verify(messageHandlerConsumer).apply(Mockito.any());
  }

  private Function<String, Message> createMessageHandlerConsumer() {
    Function<String, Message> messageHandlerConsumer = Mockito.mock(Function.class);

    Mockito.when(messageHandlerConsumer.apply(Mockito.any())).thenReturn(CommandHandlerReplyBuilder.withSuccess());

    return messageHandlerConsumer;
  }

  private CommandHandlerParams createCommandHandlerParams() {
    return new CommandHandlerParams(CommandHandlerReplyBuilder.withSuccess(), TestCommand.class, Optional.of("/TestResource"));
  }

  private ReactiveCommandHandler createReactiveCommandHandler(Function<String, Message> messageHandlerConsumer) {
   return new ReactiveCommandHandler(null, null, null, (commandMessage, pathVariables) -> Flux.just("TEST ITERATION VALUE").map(messageHandlerConsumer));
  }

  public static class TestCommand implements Command {}
}
