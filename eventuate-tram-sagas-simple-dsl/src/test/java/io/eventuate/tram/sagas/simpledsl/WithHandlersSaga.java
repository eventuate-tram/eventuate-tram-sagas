package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;


public class WithHandlersSaga implements SimpleSaga<ConditionalSagaData> {

  private SagaDefinition<ConditionalSagaData> sagaDefinition;
  private Handlers handlers;

  public WithHandlersSaga(Handlers handlers) {
    this.handlers = handlers;
    sagaDefinition = step()
        .invokeParticipant(ConditionalSagaData::isInvoke1, ConditionalSagaData::do1)
            .onReply(Failure.class, handlers::failure1)
            .onReply(Success.class, handlers::success1)
        .withCompensation(ConditionalSagaData::isInvoke1, ConditionalSagaData::undo1)
            .onReply(Success.class, handlers::compensating1)
    .step()
        .invokeParticipant(ConditionalSagaData::do2)
        .onReply(Failure.class, handlers::failure2)
        .onReply(Success.class, handlers::success2)
    .build();
  }



  @Override
  public SagaDefinition<ConditionalSagaData> getSagaDefinition() {
    return this.sagaDefinition;
  }

}
