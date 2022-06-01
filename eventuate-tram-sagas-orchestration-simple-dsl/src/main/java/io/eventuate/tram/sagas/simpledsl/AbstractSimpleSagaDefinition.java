package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class AbstractSimpleSagaDefinition<Data, Step extends ISagaStep<Data>,
        ToExecute extends AbstractStepToExecute<Data, Step>> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected List<Step> steps;

    public AbstractSimpleSagaDefinition(List<Step> steps) {
        this.steps = steps;
    }

    protected SagaActions<Data> handleFailedCompensatingTransaction(String sagaType, String sagaId, SagaExecutionState state, Message message) {
      logger.error("Saga {} {} failed due to failed compensating transaction {}", sagaType, sagaId, message);
      return SagaActions.<Data>builder()
              .withUpdatedState(SagaExecutionStateJsonSerde.encodeState(SagaExecutionState.makeFailedEndState()))
              .withIsEndState(true)
              .withIsCompensating(state.isCompensating())
              .withIsFailed(true)
              .build();
    }

    protected Optional<ToExecute> nextStepToExecute(SagaExecutionState state, Data data) {
      int skipped = 0;
      boolean compensating = state.isCompensating();
      int direction = compensating ? -1 : +1;
      for (int i = state.getCurrentlyExecuting() + direction; i >= 0 && i < steps.size(); i = i + direction) {
        Step step = steps.get(i);
        if ((compensating ? step.hasCompensation(data) : step.hasAction(data))) {
          return Optional.of(makeStepToExecute(skipped, compensating, step));
        } else
          skipped++;
      }
      return Optional.empty();
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

}
