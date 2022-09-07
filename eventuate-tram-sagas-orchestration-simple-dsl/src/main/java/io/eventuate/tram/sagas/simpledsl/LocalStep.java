package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.eventuate.tram.sagas.simpledsl.StepOutcome.makeLocalOutcome;

public class LocalStep<Data> implements SagaStep<Data> {
  private final Consumer<Data> localFunction;
  private final Optional<Consumer<Data>> compensation;
  private final List<LocalExceptionSaver<Data>> localExceptionSavers;
  private final List<Class<RuntimeException>> rollbackExceptions;

  public LocalStep(Consumer<Data> localFunction, Optional<Consumer<Data>> compensation, List<LocalExceptionSaver<Data>> localExceptionSavers, List<Class<RuntimeException>> rollbackExceptions) {
    this.localFunction = localFunction;
    this.compensation = compensation;
    this.localExceptionSavers = localExceptionSavers;
    this.rollbackExceptions = rollbackExceptions;
  }

  @Override
  public boolean hasAction(Data data) {
    return true;
  }

  @Override
  public boolean hasCompensation(Data data) {
    return compensation.isPresent();
  }


  @Override
  public boolean isSuccessfulReply(boolean compensating, Message message) {
    return CommandReplyOutcome.SUCCESS.name().equals(message.getRequiredHeader(ReplyMessageHeaders.REPLY_OUTCOME));
  }

  @Override
  public Optional<BiConsumer<Data, Object>> getReplyHandler(Message message, boolean compensating) {
    return Optional.empty();
  }


  @Override
  public StepOutcome makeStepOutcome(Data data, boolean compensating) {
    try {
      if (compensating) {
        compensation.ifPresent(localStep -> localStep.accept(data));
      } else {
        localFunction.accept(data);
      }
      return makeLocalOutcome(Optional.empty());
    } catch (RuntimeException e) {
      localExceptionSavers.stream().filter(saver -> saver.shouldSave(e)).findFirst().ifPresent(saver -> saver.save(data, e));
      if (rollbackExceptions.isEmpty() || rollbackExceptions.stream().anyMatch(c -> c.isInstance(e)))
        return makeLocalOutcome(Optional.of(e));
      else
        throw e;
    }
  }

}
