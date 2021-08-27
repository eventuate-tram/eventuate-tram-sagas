package io.eventuate.tram.sagas.reactive.simpledsl;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractReactiveParticipantInvocation<Data> implements ReactiveParticipantInvocation<Data> {

  private Optional<Predicate<Data>> invocablePredicate;

  protected AbstractReactiveParticipantInvocation(Optional<Predicate<Data>> invocablePredicate) {
    this.invocablePredicate = invocablePredicate;
  }

  @Override
  public boolean isInvocable(Data data) {
    return invocablePredicate.map(p -> p.test(data)).orElse(true);
  }
}
