package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SagaCommandProducerImpl implements SagaCommandProducer {

  private CommandProducer commandProducer;

  public SagaCommandProducerImpl(CommandProducer commandProducer) {
    this.commandProducer = commandProducer;
  }

  @Override
  public String sendCommands(String sagaType, String sagaId, List<CommandWithDestinationAndType> commands, String sagaReplyChannel) {
    String messageId = null;
    for (CommandWithDestinationAndType cwdt : commands) {
      CommandWithDestination command = cwdt.getCommandWithDestination();
      Map<String, String> headers = new HashMap<>(command.getExtraHeaders());
      headers.put(SagaCommandHeaders.SAGA_TYPE, sagaType);
      headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
      if (cwdt.isNotification())
        messageId = commandProducer.sendNotification(command.getDestinationChannel(), command.getCommand(), headers);
      else
        messageId = commandProducer.send(command.getDestinationChannel(), command.getResource(), command.getCommand(), sagaReplyChannel, headers);
    }
    return messageId;

  }
}
