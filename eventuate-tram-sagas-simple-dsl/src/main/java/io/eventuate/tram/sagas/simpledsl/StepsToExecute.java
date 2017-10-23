package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.consumer.CommandWithDestination;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class StepsToExecute<Data> {
  private final List<LocalStep<Data>> localSteps;
  private final Optional<ParticipantInvocationStep<Data>> nonLocalStep;
  private int skipped;


  public StepsToExecute(List<LocalStep<Data>> localSteps, Optional<ParticipantInvocationStep<Data>> nonLocalStep, int skipped) {
    this.localSteps = localSteps;
    this.nonLocalStep = nonLocalStep;
    this.skipped = skipped;
  }

  public List<CommandWithDestination> makeCommandsToSend(Data data, boolean compensating) {
    List<CommandWithDestination> commands = new LinkedList<>();
    nonLocalStep.flatMap(pi -> makeCommandToSend(pi, compensating, data)).ifPresent(commands::add);
    return commands;
  }

  private Optional<CommandWithDestination> makeCommandToSend(ParticipantInvocationStep<Data> pis, boolean compensating, Data data) {
    if (compensating) {
      Optional<ParticipantInvocation<Data>> pi = pis.getCompensatingParticipantInvocation();
      return pi.map(x -> x.makeCommandToSend(data));
    } else {
      Optional<ParticipantInvocation<Data>> pi = pis.getAction();
      return pi.map(x -> x.makeCommandToSend(data));
    }
  }

  public int size() {
    return localSteps.size() + nonLocalStep.map(nls -> 1).orElse(0) + skipped;
  }

  public void executeLocalSteps(Data data, boolean compensating) {
    for (LocalStep<Data> step : localSteps) {
      step.execute(data, compensating);
    }
  }

  public boolean isEmpty() {
    return localSteps.isEmpty() && !nonLocalStep.isPresent();
  }
}
