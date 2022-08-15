package io.eventuate.tram.sagas.testing;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.consumer.MessageConsumer;
import io.eventuate.tram.sagas.testing.commandhandling.ReconfigurableCommandHandlers;
import io.eventuate.tram.sagas.testing.commandhandling.SagaParticipantStubCommandHandler;
import io.eventuate.tram.sagas.testing.commandhandling.UnhandledMessageTrackingCommandDispatcher;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

/**
 * Defines a DSL for configuring stubbed saga participants (more generally, recipients of command messages) that response to command messages.
 * WireMock-equivalent for Tram messaging
 */
public class SagaParticipantStubManager {
  private final ReconfigurableCommandHandlers commandHandlers;
  private final Set<String> commandChannels;
  private final UnhandledMessageTrackingCommandDispatcher commandDispatcher;
  private String currentCommandChannel;


  public SagaParticipantStubManager(SagaParticipantChannels sagaParticipantChannels,
                                    MessageConsumer messageConsumer,
                                    CommandNameMapping commandNameMapping, CommandReplyProducer commandReplyProducer) {
    this.commandChannels = sagaParticipantChannels.getChannels();
    this.commandHandlers = new ReconfigurableCommandHandlers(this.commandChannels);
    this.commandDispatcher = new UnhandledMessageTrackingCommandDispatcher("SagaParticipantStubManager-command-dispatcher-" + System.currentTimeMillis(),
            commandHandlers,
            messageConsumer,
            commandNameMapping, commandReplyProducer);

    /// TODO handle scenario where a command is recieved for which there is not a handler.
  }


  @PostConstruct
  public void initialize() {
    commandDispatcher.initialize();
  }

  public void reset() {
    commandHandlers.reset();
    commandDispatcher.reset();

  }

  public SagaParticipantStubManager forChannel(String commandChannel) {
    validateChannel(commandChannel);
    this.currentCommandChannel = commandChannel;
    return this;
  }

  private void validateChannel(String commandChannel) {
    if (!commandChannels.contains(commandChannel))
      throw new IllegalArgumentException(String.format("%s is not one of the specified channels: %s", commandChannel, commandChannels));
  }

  public <C extends Command> SagaParticipantStubManagerHelper<C> when(C expectedCommand) {
    return new SagaParticipantStubManagerHelper<C>(this, (Class<C>) expectedCommand.getClass(),
            message -> JSonMapper.fromJson(message.getPayload(), expectedCommand.getClass()).equals(expectedCommand));
  }

  public <C extends Command> SagaParticipantStubManagerHelper<C> when(Class<C> expectedCommandClass) {
    return new SagaParticipantStubManagerHelper<C>(this, expectedCommandClass, message -> true);
  }

  public <C extends Command> void verifyCommandReceived(String channel, Class<C> commandClass) {
    commandHandlers.findCommandHandler(channel, commandClass)
            .map(handler -> {
              handler.verifyCommandReceived();
              return true;
            })
            .orElseThrow(() -> new RuntimeException(String.format("no handler for channel %s command class %s", channel, commandClass)));

  }


  public class SagaParticipantStubManagerHelper<C extends Command>  {
    private Class<C> expectedCommandClass;
    private final Predicate<Message> expectedCommand;
    private SagaParticipantStubManager sagaParticipantStubManager;

    public SagaParticipantStubManagerHelper(SagaParticipantStubManager sagaParticipantStubManager, Class<C> expectedCommandClass, Predicate<Message> expectedCommand) {
      this.sagaParticipantStubManager = sagaParticipantStubManager;
      this.expectedCommandClass = expectedCommandClass;
      this.expectedCommand = expectedCommand;
    }

    public SagaParticipantStubManager replyWith(Function<CommandMessage<C>, Message> replyBuilder) {
      SagaParticipantStubCommandHandler<C> commandHandler = new SagaParticipantStubCommandHandler<>(currentCommandChannel, expectedCommandClass, expectedCommand, replyBuilder);
      sagaParticipantStubManager.commandHandlers.add(commandHandler);
      commandDispatcher.noteNewCommandHandler(commandHandler);
      return sagaParticipantStubManager;
    }

    public SagaParticipantStubManager replyWithSuccess() {
      return replyWith(cm -> withSuccess());
    }

    public SagaParticipantStubManager replyWithFailure() {
      return replyWith(cm -> withFailure());
    }
  }

}
