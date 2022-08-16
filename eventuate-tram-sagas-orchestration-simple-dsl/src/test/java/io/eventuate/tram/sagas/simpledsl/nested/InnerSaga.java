package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

public class InnerSaga implements SimpleSaga<InnerSagaData>  {
    private final SagaDefinition<InnerSagaData> sagaDefinition;

    private final CommandReplyProducer commandReplyProducer;

    public InnerSaga(CommandReplyProducer commandReplyProducer) {
        this.commandReplyProducer = commandReplyProducer;
        this.sagaDefinition = step()
                .invokeParticipant(InnerSagaData::innerOperation)
            .build()
         ;
    }


    @Override
    public SagaDefinition<InnerSagaData> getSagaDefinition() {
        return this.sagaDefinition;
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, InnerSagaData innerSagaData) {
        SimpleSaga.super.onSagaCompletedSuccessfully(sagaId, innerSagaData);
        commandReplyProducer.sendReplies(innerSagaData.getCommandReplyToken(), withSuccess());
    }
}
