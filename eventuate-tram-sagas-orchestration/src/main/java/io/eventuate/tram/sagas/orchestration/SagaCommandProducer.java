package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SagaCommandProducer {

  private CommandProducer commandProducer;

  public SagaCommandProducer(CommandProducer commandProducer) {
    this.commandProducer = commandProducer;
  }

  public String sendCommands(String sagaType, String sagaId, List<CommandWithDestination> commands, String sagaReplyChannel) {
    String messageId = null;
    for (CommandWithDestination command : commands) {
      Map<String, String> headers = new HashMap<>(command.getExtraHeaders());
      headers.put(SagaCommandHeaders.SAGA_TYPE, sagaType);
      headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
      messageId = commandProducer.send(command.getDestinationChannel(), command.getResource(), command.getCommand(), sagaReplyChannel, headers);
    }
    return messageId;

  }
}
