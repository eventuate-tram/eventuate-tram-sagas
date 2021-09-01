package io.eventuate.tram.sagas.reactive.simpledsl;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionState;
import io.eventuate.tram.sagas.simpledsl.SagaExecutionStateJsonSerde;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class SimpleReactiveSagaDefinition<Data> implements ReactiveSagaDefinition<Data> {
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private List<ReactiveSagaStep<Data>> sagaSteps;

  public SimpleReactiveSagaDefinition(List<ReactiveSagaStep<Data>> sagaSteps) {
    this.sagaSteps = sagaSteps;
  }

  @Override
  public Publisher<SagaActions<Data>> start(Data sagaData) {
    SagaExecutionState currentState = new SagaExecutionState(-1, false);

    ReactiveStepToExecute<Data> stepToExecute = nextStepToExecute(currentState, sagaData);

    if (stepToExecute.isEmpty()) {
      return Mono.just(makeEndStateSagaActions(currentState));
    } else {
      return stepToExecute.executeStep(sagaData, currentState);
    }
  }

  @Override
  public Publisher<SagaActions<Data>> handleReply(String currentState, Data sagaData, Message message) {

    SagaExecutionState state = SagaExecutionStateJsonSerde.decodeState(currentState);
    ReactiveSagaStep<Data> currentStep = sagaSteps.get(state.getCurrentlyExecuting());
    boolean compensating = state.isCompensating();

    return currentStep
            .getReplyHandler(message, compensating)
            .map(handler -> Mono.from(invokeReplyHandler(message, sagaData, handler)))
            .orElse(Mono.empty())
            .then(Mono.defer(() -> {
              if (currentStep.isSuccessfulReply(compensating, message)) {
                return Mono.from(executeNextStep(sagaData, state));
              } else if (compensating) {
                return Mono.error(new UnsupportedOperationException("Failure when compensating"));
              } else {
                return Mono.from(executeNextStep(sagaData, state.startCompensating()));
              }
            }));
  }



  private ReactiveStepToExecute<Data> nextStepToExecute(SagaExecutionState state, Data data) {
    int skipped = 0;
    boolean compensating = state.isCompensating();
    int direction = compensating ? -1 : +1;
    for (int i = state.getCurrentlyExecuting() + direction; i >= 0 && i < sagaSteps.size(); i = i + direction) {
      ReactiveSagaStep<Data> step = sagaSteps.get(i);
      if ((compensating ? step.hasCompensation(data) : step.hasAction(data))) {
        return new ReactiveStepToExecute<>(Optional.of(step), skipped, compensating);
      } else
        skipped++;
    }
    return new ReactiveStepToExecute<>(Optional.empty(), skipped, compensating);
  }

  private Publisher<SagaActions<Data>> executeNextStep(Data data, SagaExecutionState state) {
    ReactiveStepToExecute<Data> stepToExecute = nextStepToExecute(state, data);

    if (stepToExecute.isEmpty()) {
      return Mono.just(makeEndStateSagaActions(state));
    } else {
      // do something
      return stepToExecute.executeStep(data, state);
    }
  }

  private Publisher<?> invokeReplyHandler(Message message, Data data, BiFunction<Data, Object, Publisher<?>> handler) {
    Class m;
    try {
      String className = message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE);
      m = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
    } catch (ClassNotFoundException e) {
      logger.error("Class not found", e);
      throw new RuntimeException("Class not found", e);
    }
    Object reply = JSonMapper.fromJson(message.getPayload(), m);
    return handler.apply(data, reply);
  }

  private SagaActions<Data> makeEndStateSagaActions(SagaExecutionState state) {
    return SagaActions.<Data>builder()
            .withUpdatedState(SagaExecutionStateJsonSerde.encodeState(SagaExecutionState.makeEndState()))
            .withIsEndState(true)
            .withIsCompensating(state.isCompensating())
            .build();
  }
}
