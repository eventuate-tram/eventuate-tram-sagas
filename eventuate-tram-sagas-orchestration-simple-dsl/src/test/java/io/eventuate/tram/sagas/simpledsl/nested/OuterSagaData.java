package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.simpledsl.ReleaseCreditCommand;
import io.eventuate.tram.sagas.simpledsl.ReserveCreditCommand;

public class OuterSagaData {
    public CommandWithDestination reserveCredit() {
        return new CommandWithDestination("customerService", null, new ReserveCreditCommand());
    }

    public CommandWithDestination releaseCredit() {
        return new CommandWithDestination("customerService", null, new ReleaseCreditCommand());
    }

    public void approveOrder() {
    }
}
