package io.eventuate.tram.sagas.simpledsl;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractParticipantInvocation<Data> implements ParticipantInvocation<Data> {

  private Optional<Predicate<Data>> invocablePredicate;

  protected AbstractParticipantInvocation(Optional<Predicate<Data>> invocablePredicate) {
    this.invocablePredicate = invocablePredicate;
  }

  @Override
  public boolean isInvocable(Data data) {
    return invocablePredicate.map(p -> p.test(data)).orElse(true);
  }
}
