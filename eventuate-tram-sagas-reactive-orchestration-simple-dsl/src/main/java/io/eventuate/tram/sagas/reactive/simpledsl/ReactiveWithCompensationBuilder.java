package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import org.reactivestreams.Publisher;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ReactiveWithCompensationBuilder<Data> {

  InvokeReactiveParticipantStepBuilder<Data> withCompensation(Function<Data, Publisher<CommandWithDestination>> compensation);

  InvokeReactiveParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate,
                                                              Function<Data, Publisher<CommandWithDestination>> compensation);

  <C extends Command> InvokeReactiveParticipantStepBuilder<Data> withCompensation(CommandEndpoint<C> commandEndpoint,
                                                                                  Function<Data, Publisher<C>> commandProvider);

  <C extends Command> InvokeReactiveParticipantStepBuilder<Data> withCompensation(Predicate<Data> compensationPredicate,
                                                                                  CommandEndpoint<C> commandEndpoint,
                                                                                  Function<Data, Publisher<C>> commandProvider);
}
