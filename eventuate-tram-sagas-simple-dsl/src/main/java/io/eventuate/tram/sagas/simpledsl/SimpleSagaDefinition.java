package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.RawSagaStateMachineAction;
import io.eventuate.tram.sagas.orchestration.ReplyClassAndHandler;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.orchestration.StartingHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SimpleSagaDefinition<Data> implements SagaDefinition<Data> {
  private List<SagaStep<Data>> sagaSteps;

  public SimpleSagaDefinition(List<SagaStep<Data>> sagaSteps) {
    this.sagaSteps = sagaSteps;
  }

  @Override
  public SagaActions<Data> start(Data sagaData) {
    return getStartingHandler().get().apply(sagaData);
  }

  @Override
  public SagaActions<Data> handleReply(String currentState, Data sagaData, Message message) {
    Optional<ReplyClassAndHandler> replyHandler = this.findReplyHandler(currentState, sagaData, message);

//    if (!replyHandler.isPresent()) {
//      logger.error("No handler for {}", message);
//      return;
//    }
    ReplyClassAndHandler m = replyHandler.get();

    Object param = JSonMapper.fromJson(message.getPayload(), m.getReplyClass());

    return (SagaActions<Data>) m.getReplyHandler().apply(sagaData, param);
  }

  public Optional<StartingHandler<Data>> getStartingHandler() {
    return Optional.of(this::startingHandler);
  }

  private SagaActions startingHandler(Data data) {
    SagaExecutionState currentState = new SagaExecutionState(-1, false);

    StepsToExecute<Data> stepsToExecute = nextStepsToExecute(currentState);
    SagaExecutionState newState = currentState.nextState(stepsToExecute.size());

    return makeSagaActions(data, stepsToExecute, currentState, newState);
  }

  private SagaActions makeSagaActions(final Data data, final StepsToExecute<Data> stepsToExecute, SagaExecutionState currentState, SagaExecutionState newState) {
    stepsToExecute.executeLocalSteps(data, currentState.isCompensating());
    // TODO What to do if the above fails

    return SagaActions.builder()
            .withCommands(stepsToExecute.makeCommandsToSend(data, currentState.isCompensating()))
            .withUpdatedSagaData(data)
            .withUpdatedState(encodeState(newState))
            .withIsEndState(newState.isEndState())
            .withIsCompensating(currentState.isCompensating())
            .build();

  }


  private StepsToExecute<Data> nextStepsToExecute(SagaExecutionState state) {
    List<LocalStep<Data>> localSteps = new LinkedList<>();

    int skipped = 0;
    if (state.isCompensating()) {
      int i = state.getCurrentlyExecuting() - 1;
      while (i >= 0) {
        SagaStep<Data> step = sagaSteps.get(i--);
        if (step instanceof LocalStep) {
          localSteps.add((LocalStep<Data>) step);
        } else if (step instanceof ParticipantInvocationStep && ((ParticipantInvocationStep)step).hasCompensation()) {
          return new StepsToExecute<>(localSteps, Optional.of((ParticipantInvocationStep) step), skipped);
        } else
          skipped++;
      }
      return new StepsToExecute<>(localSteps, Optional.empty(), skipped);

    } else {
      int i = state.getCurrentlyExecuting() + 1;
      while (i < sagaSteps.size()) {
        SagaStep<Data> step = sagaSteps.get(i++);
        if (step instanceof LocalStep) {
          localSteps.add((LocalStep<Data>) step);
        } else if (step instanceof ParticipantInvocationStep && ((ParticipantInvocationStep)step).hasAction()) {
          return new StepsToExecute<>(localSteps, Optional.of((ParticipantInvocationStep) step), skipped);
        } else
          skipped++;
      }
      return new StepsToExecute<>(localSteps, Optional.empty(), skipped);
    }
  }

  public Optional<ReplyClassAndHandler> findReplyHandler(String currentState, Data data, Message message) {
    SagaExecutionState state = decodeState(currentState);
    ParticipantInvocationStep<Data> participantInvocationStep = participantInvocationStepFor(state);
    String replyType = message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE);
    Optional<BiConsumer<Data, Object>> possibleReplyHandler = participantInvocationStep.getReplyHandler(replyType, state.isCompensating());
    ParticipantInvocation pi = participantInvocationStep.getParticipantInvocation(state.isCompensating());
    if (pi.isSuccessfulReply(message)) {
      return figureOutNextStepsAndState(data, replyType, state, possibleReplyHandler);
    } else if (state.isCompensating()) {
      throw new UnsupportedOperationException("Failure when compensating");
    } else {
      return figureOutNextStepsAndState(data, replyType, state.startCompensating(), possibleReplyHandler);
    }
  }

  private ParticipantInvocationStep<Data> participantInvocationStepFor(SagaExecutionState state) {
    return (ParticipantInvocationStep<Data>) sagaSteps.get(state.getCurrentlyExecuting());
  }

  private static SagaExecutionState decodeState(String currentState) {
    return JSonMapper.fromJson(currentState, SagaExecutionState.class);
  }

  private static String encodeState(SagaExecutionState state) {
    return JSonMapper.toJson(state);
  }

  private Optional<ReplyClassAndHandler> figureOutNextStepsAndState(final Data data, final String messageType, final SagaExecutionState state, Optional<BiConsumer<Data, Object>> possibleReplyHandler) {
    StepsToExecute<Data> stepsToExecute = nextStepsToExecute(state);
    return Optional.of(new ReplyClassAndHandler() {
      @Override
      public RawSagaStateMachineAction getReplyHandler() {
        return (rawSagaData, reply) -> {
          possibleReplyHandler.ifPresent(handler -> handler.accept(data, reply));
          if (stepsToExecute.isEmpty()) {
            return SagaActions.builder()
                    .withUpdatedState(encodeState(SagaExecutionState.makeEndState()))
                    .withIsEndState(true)
                    .withIsCompensating(state.isCompensating())
                    .build();
          } else {
            // do something
            return makeSagaActions(data, stepsToExecute, state, state.nextState(stepsToExecute.size()));
          }
        };
      }

      @Override
      public Class<?> getReplyClass() {
        try {
          return Class.forName(messageType);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }




}
