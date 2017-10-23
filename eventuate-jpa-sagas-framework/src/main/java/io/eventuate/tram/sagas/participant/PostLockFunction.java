package io.eventuate.tram.sagas.participant;

import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.PathVariables;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.common.LockTarget;

public interface PostLockFunction<C> {

  public LockTarget apply(CommandMessage<C> cm, PathVariables pvs, Message reply);
}
