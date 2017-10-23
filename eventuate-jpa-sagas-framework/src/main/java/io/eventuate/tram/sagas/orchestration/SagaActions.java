package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SagaActions<Data> {


  List<CommandWithDestination> getCommands();

  Optional<Data> getUpdatedSagaData();

  Optional<String> getUpdatedState();

  Set<EnlistedAggregate> getEnlistedAggregates();

  Set<EventToPublish> getEventsToPublish();
}
