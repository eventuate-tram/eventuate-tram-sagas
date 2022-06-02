package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiFunction;

public abstract class AbstractSimpleSagaDefinition<Data, Step extends ISagaStep<Data>,
        ToExecute extends AbstractStepToExecute<Data, Step>,
        Provider extends AbstractSagaActionsProvider<Data,?>> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected List<Step> steps;

    public AbstractSimpleSagaDefinition(List<Step> steps) {
        this.steps = steps;
    }

    protected Provider firstStepToExecute(Data data) {
        return nextStepToExecute(SagaExecutionState.startingState(), data);
    }

    protected Provider handleFailedCompensatingTransaction(String sagaType, String sagaId, SagaExecutionState state, Message message) {
        logger.error("Saga {} {} failed due to failed compensating transaction {}", sagaType, sagaId, message);
        return makeSagaActionsProvider(SagaActions.<Data>builder()
                .withUpdatedState(SagaExecutionStateJsonSerde.encodeState(SagaExecutionState.makeFailedEndState()))
                .withIsEndState(true)
                .withIsCompensating(state.isCompensating())
                .withIsFailed(true)
                .build());
    }

    protected Provider sagaActionsForNextStep(String sagaType, String sagaId, Data sagaData, Message message,
                                              SagaExecutionState state, Step currentStep, boolean compensating) {
        if (currentStep.isSuccessfulReply(compensating, message)) {
            return nextStepToExecute(state, sagaData);
        } else if (compensating) {
            return handleFailedCompensatingTransaction(sagaType, sagaId, state, message);
        } else {
            return nextStepToExecute(state.startCompensating(), sagaData);
        }
    }

    protected Provider nextStepToExecute(SagaExecutionState state, Data data) {
        int skipped = 0;
        boolean compensating = state.isCompensating();
        int direction = compensating ? -1 : +1;
        for (int i = state.getCurrentlyExecuting() + direction; i >= 0 && i < steps.size(); i = i + direction) {
            Step step = steps.get(i);
            if ((compensating ? step.hasCompensation(data) : step.hasAction(data))) {
                ToExecute stepToExecute = makeStepToExecute(skipped, compensating, step);
                return makeSagaActionsProvider(stepToExecute, data, state);
            } else
                skipped++;
        }
        return makeSagaActionsProvider(makeEndStateSagaActions(state));
    }

    protected <T> T invokeReplyHandler(Message message, Data data, BiFunction<Data, Object, T> handler) {
        Class<?> m;
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

    protected SagaActions<Data> makeEndStateSagaActions(SagaExecutionState state) {
        return SagaActions.<Data>builder()
                .withUpdatedState(SagaExecutionStateJsonSerde.encodeState(SagaExecutionState.makeEndState()))
                .withIsEndState(true)
                .withIsCompensating(state.isCompensating())
                .build();
    }

    protected abstract ToExecute makeStepToExecute(int skipped, boolean compensating, Step step);

    protected abstract Provider makeSagaActionsProvider(SagaActions<Data> sagaActions);

    protected abstract Provider makeSagaActionsProvider(ToExecute stepToExecute, Data data, SagaExecutionState state);


}
