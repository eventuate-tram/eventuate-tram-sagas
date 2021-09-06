package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public abstract class AbstractSagaDefinition<SAGA_STEP extends AbstractSagaStep<SAGA_DATA>, SAGA_ACTIONS, SAGA_DATA>
				implements SagaDefinition<SAGA_ACTIONS, SAGA_DATA> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected List<SAGA_STEP> sagaSteps;

	public AbstractSagaDefinition(List<SAGA_STEP> sagaSteps) {
		this.sagaSteps = sagaSteps;
	}

	@Override
	public SAGA_ACTIONS start(SAGA_DATA sagaData) {
		SagaExecutionState currentState = new SagaExecutionState(-1, false);

		AbstractStepToExecute<SAGA_STEP, SAGA_ACTIONS, SAGA_DATA> stepToExecute =
						nextStepToExecute(currentState, sagaData);

		if (stepToExecute.isEmpty()) {
			return actualSagaActionsFromPureSagaActions(makeEndStateSagaActions(currentState));
		} else
			return stepToExecute.executeStep(sagaData, currentState);
	}

	@Override
	public SAGA_ACTIONS handleReply(String currentState, SAGA_DATA sagaData, Message message) {
		SagaExecutionState state = SagaExecutionStateJsonSerde.decodeState(currentState);
		SAGA_STEP currentStep = sagaSteps.get(state.getCurrentlyExecuting());
		boolean compensating = state.isCompensating();

		return executeReplyStep(currentStep, state, message, sagaData, compensating);
	}


	protected Object prepareReply(Message message) {
		Class m;
		try {
			String className = message.getRequiredHeader(ReplyMessageHeaders.REPLY_TYPE);
			m = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			logger.error("Class not found", e);
			throw new RuntimeException("Class not found", e);
		}
		return JSonMapper.fromJson(message.getPayload(), m);
	}

	protected SagaActions<SAGA_DATA> makeEndStateSagaActions(SagaExecutionState state) {
		return SagaActions.<SAGA_DATA>builder()
						.withUpdatedState(SagaExecutionStateJsonSerde.encodeState(SagaExecutionState.makeEndState()))
						.withIsEndState(true)
						.withIsCompensating(state.isCompensating())
						.build();
	}

	protected SAGA_ACTIONS executeNextStep(SAGA_DATA data, SagaExecutionState state) {
		AbstractStepToExecute<SAGA_STEP, SAGA_ACTIONS, SAGA_DATA> stepToExecute =
						nextStepToExecute(state, data);

		if (stepToExecute.isEmpty()) {
			return actualSagaActionsFromPureSagaActions(makeEndStateSagaActions(state));
		} else {
			// do something
			return stepToExecute.executeStep(data, state);
		}
	}

	protected AbstractStepToExecute<SAGA_STEP, SAGA_ACTIONS, SAGA_DATA> nextStepToExecute(SagaExecutionState state, SAGA_DATA data) {
		int skipped = 0;
		boolean compensating = state.isCompensating();
		int direction = compensating ? -1 : +1;
		for (int i = state.getCurrentlyExecuting() + direction; i >= 0 && i < sagaSteps.size(); i = i + direction) {
			SAGA_STEP step = sagaSteps.get(i);
			if ((compensating ? step.hasCompensation(data) : step.hasAction(data))) {
				return createStepToExecute(Optional.of(step), skipped, compensating);
			} else
				skipped++;
		}
		return createStepToExecute(Optional.empty(), skipped, compensating);
	}

	protected abstract AbstractStepToExecute<SAGA_STEP, SAGA_ACTIONS, SAGA_DATA> createStepToExecute(Optional<SAGA_STEP> step,
																																																	 int skipped,
																																																	 boolean compensating);

	protected abstract SAGA_ACTIONS executeReplyStep(SAGA_STEP currentStep,
																									 SagaExecutionState state,
																									 Message message,
																									 SAGA_DATA sagaData,
																									 boolean compensating);

	protected abstract SAGA_ACTIONS actualSagaActionsFromPureSagaActions(SagaActions<SAGA_DATA> sagaActions);
}
