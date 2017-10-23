package io.eventuate.tram.sagas.eventsourcingsupport;

import io.eventuate.AggregateRepositoryInterceptor;
import io.eventuate.CommandProcessingAggregate;
import io.eventuate.Event;
import io.eventuate.UpdateEventsAndOptions;
import io.eventuate.tram.commands.consumer.CommandMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class CommandMessageAggregateRepositoryInterceptor implements AggregateRepositoryInterceptor {
  private CommandMessage commandMessage;

  public CommandMessageAggregateRepositoryInterceptor(CommandMessage commandMessage) {
    this.commandMessage = commandMessage;
  }

  @Override
  public UpdateEventsAndOptions transformUpdate(CommandProcessingAggregate aggregate, UpdateEventsAndOptions ueo) {
    return withEvents(ueo, singletonList(makeSagaReplyRequestedEvent(commandMessage)));
  }

  private UpdateEventsAndOptions withEvents(UpdateEventsAndOptions ueo, List<SagaReplyRequestedEvent> moreEvents) {
    List<Event> newEvents = new ArrayList<>(ueo.getEvents());
    newEvents.addAll(moreEvents);
    return new UpdateEventsAndOptions(newEvents, ueo.getOptions());
  }

  private SagaReplyRequestedEvent makeSagaReplyRequestedEvent(CommandMessage commandMessage) {
    return new SagaReplyRequestedEvent(commandMessage.getCorrelationHeaders());
  }


  @Override
  public Optional<UpdateEventsAndOptions> handleException(CommandProcessingAggregate aggregate, Throwable throwable, Optional optional) {
    throw new UnsupportedOperationException();
  }
}
