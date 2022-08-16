package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import io.eventuate.tram.sagas.participant.SagaCommandHandlersBuilder;
import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

public class ParticipantCommandHandlers {

    private final SagaInstanceFactory sagaInstanceFactory;
    private final InnerSaga innerSaga;

    public ParticipantCommandHandlers(SagaInstanceFactory sagaInstanceFactory, InnerSaga innerSaga) {
        this.sagaInstanceFactory = sagaInstanceFactory;
        this.innerSaga = innerSaga;
    }

    public CommandHandlers commandHandlerDefinitions() {
        return SagaCommandHandlersBuilder
                .fromChannel("customerService")
                .onMessage(ReserveCreditCommand.class, this::reserveCredit)
                .onMessage(ReleaseCreditCommand.class, this::releaseCredit)
                .onMessage(InnerCommand.class, this::innerCommand)
                // CommandHandler for inner saga - dummy
                .build();
    }

    private Message innerCommand(CommandMessage<InnerCommand> innerCommandCommandMessage) {
        return withSuccess();
    }

    private void reserveCredit(CommandMessage<ReserveCreditCommand> cm, CommandReplyToken commandReplyToken) {
        InnerSagaData data = new InnerSagaData(commandReplyToken);
        sagaInstanceFactory.create(innerSaga, data);
    }

    private Message releaseCredit(CommandMessage<ReleaseCreditCommand> releaseCreditCommandCommandMessage) {
        return withSuccess();
    }

}
