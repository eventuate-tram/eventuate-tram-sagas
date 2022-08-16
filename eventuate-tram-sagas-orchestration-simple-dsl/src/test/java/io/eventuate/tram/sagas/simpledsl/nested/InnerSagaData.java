package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.commands.consumer.CommandReplyToken;
import io.eventuate.tram.commands.consumer.CommandWithDestination;

public class InnerSagaData {
    private CommandReplyToken commandReplyToken;

    public InnerSagaData(CommandReplyToken commandReplyToken) {
        this.commandReplyToken = commandReplyToken;
    }

    private InnerSagaData() {
    }

    public CommandWithDestination innerOperation() {
        return new CommandWithDestination("customerService", null, new InnerCommand());
    }

    public CommandReplyToken getCommandReplyToken() {
        return commandReplyToken;
    }

    public void setCommandReplyToken(CommandReplyToken commandReplyToken) {
        this.commandReplyToken = commandReplyToken;
    }
}
