package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.javaclient.commonimpl.JSonMapper;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.RawSagaStateMachineAction;
import io.eventuate.tram.sagas.orchestration.ReplyClassAndHandler;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;

import java.util.function.BiConsumer;

public class SimpleSagaDefinition<Data> implements SagaDefinition<Data> {

    private final StepCoordinator<Data> stepCoordinator;

    public SimpleSagaDefinition(final StepCoordinator<Data> stepCoordinator) {
        this.stepCoordinator = stepCoordinator;
    }


    @Override
    public SagaActions<Data> getActions(final Data sagaData) {
        return startInternal(sagaData);
    }

    @Override
    public SagaActions<Data> getReplyActions(final String currentState,
                                             final Data sagaData,
                                             final Message message) {
        final ReplyClassAndHandler<Data> replyHandler = findReplyHandler(currentState, sagaData, message);

        final Object param = JSonMapper.fromJson(message.getPayload(), replyHandler.getReplyClass());

        return replyHandler.getReplyHandler().apply(sagaData, param);
    }

    private SagaActions<Data> startInternal(final Data data) {
        final SagaExecutionState currentState = new SagaExecutionState(-1, false, false);

        final StepsToExecute<Data> stepsToExecute = stepCoordinator.nextStepsToExecute(currentState);
        final SagaExecutionState newState = currentState.nextState(stepsToExecute.size());
        executeLocalSteps(data, stepsToExecute, currentState);

        return SagaActions.<Data>builder()
                .withCommands(stepsToExecute.makeCommandsToSend(data, currentState.isCompensating()))
                .withUpdatedSagaData(data)
                .withUpdatedState(encodeState(newState))
                .withIsEndState(newState.isEndState())
                .withIsCompensating(currentState.isCompensating())
                .build();
    }


    private ReplyClassAndHandler<Data> findReplyHandler(final String currentState, final Data data, Message message) {
        final SagaExecutionState state = decodeState(currentState);
        final ParticipantInvocationStep<Data> participantInvocationStep = stepCoordinator.participantInvocationStepFor(state);

        final String replyType = message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE);

        final BiConsumer<Data, Object> replyHandler = participantInvocationStep.getReplyHandler(replyType, state.isCompensating())
                .orElse((data1, o) -> {
                });

        ParticipantInvocation pi = participantInvocationStep.getParticipantInvocation(state.isCompensating());
        if (pi.isSuccessfulReply(message)) {
            return figureOutNextStepsAndState(data, replyType, state, replyHandler);
        } else if (state.isCompensating()) {
            throw new UnsupportedOperationException("Failure when compensating");
        } else {
            return figureOutNextStepsAndState(data, replyType, state.startCompensating(), replyHandler);
        }
    }


    private static SagaExecutionState decodeState(final String currentState) {
        return JSonMapper.fromJson(currentState, SagaExecutionState.class);
    }

    private static String encodeState(SagaExecutionState state) {
        return JSonMapper.toJson(state);
    }

    private ReplyClassAndHandler<Data> figureOutNextStepsAndState(final Data data,
                                                                  final String messageType,
                                                                  final SagaExecutionState currentState,
                                                                  final BiConsumer<Data, Object> possibleReplyHandler) {
        return new ReplyClassAndHandler<Data>() {
            @Override
            public RawSagaStateMachineAction<Data> getReplyHandler() {

                final StepsToExecute<Data> stepsToExecute = stepCoordinator.nextStepsToExecute(currentState);
                return (sagaData, reply) -> {
                    possibleReplyHandler.accept(data, reply);
                    if (stepsToExecute.isEmpty()) {
                        return SagaActions.<Data>builder()
                                .withUpdatedState(encodeState(SagaExecutionState.makeEndState()))
                                .withIsEndState(true)
                                .withIsCompensating(currentState.isCompensating())
                                .build();
                    } else {
                        executeLocalSteps(data, stepsToExecute, currentState);
                        final SagaExecutionState newState = currentState.nextState(stepsToExecute.size());

                        return SagaActions.<Data>builder()
                                .withCommands(stepsToExecute.makeCommandsToSend(data, currentState.isCompensating()))
                                .withUpdatedSagaData(data)
                                .withUpdatedState(encodeState(newState))
                                .withIsEndState(newState.isEndState())
                                .withIsCompensating(currentState.isCompensating())
                                .build();
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
        };
    }


    private void executeLocalSteps(final Data data, final StepsToExecute<Data> stepsToExecute, final SagaExecutionState currentState) {
        stepsToExecute.executeLocalSteps(data, currentState.isCompensating());
        // TODO What to do if the above fails

    }

}
