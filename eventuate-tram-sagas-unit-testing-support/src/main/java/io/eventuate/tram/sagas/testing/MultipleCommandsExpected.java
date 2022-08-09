package io.eventuate.tram.sagas.testing;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.CommandWithDestinationAndType;

import java.util.LinkedList;
import java.util.List;

public class MultipleCommandsExpected<T> {
    private final SagaUnitTestSupport<T> sagaUnitTestSupport;
    private boolean notification;
    private Command command;

    private final List<CommandWithDestinationAndType> commandsAndNotifications = new LinkedList<>();

    public MultipleCommandsExpected(SagaUnitTestSupport<T> sagaUnitTestSupport) {
        super();
        this.sagaUnitTestSupport = sagaUnitTestSupport;
    }


    public MultipleCommandsExpected<T> notification(Command notification) {
        this.notification = true;
        this.command = notification;
        return this;
    }

    public MultipleCommandsExpected<T> to(String channel) {
        commandsAndNotifications.add(new CommandWithDestinationAndType(new CommandWithDestination(channel, null, command), notification));
        return this;
    }

    public SagaUnitTestSupport<T> verify() {
        sagaUnitTestSupport.verifySent(commandsAndNotifications);
        return sagaUnitTestSupport;
    }

    public MultipleCommandsExpected<T> command(Command command) {
        this.notification = false;
        this.command = command;
        return this;
    }
}
