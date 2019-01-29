package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SagaCommandProducer {

  @Autowired
  private CommandProducer commandProducer;

  public SagaCommandProducer() {
  }

  public SagaCommandProducer(CommandProducer commandProducer) {
    this.commandProducer = commandProducer;
  }



  public String sendCommand(String sagaType, String sagaId, String destinationChannel, String resource, Command command, String replyTo) {
    Map<String, String> headers = new HashMap<>();
    headers.put(SagaCommandHeaders.SAGA_TYPE, sagaType);
    headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
    return commandProducer.send(destinationChannel, resource, command, replyTo, headers);
  }

  public String sendCommands(String sagaType, String sagaId, List<CommandWithDestination> commands, String sagaReplyChannel) {
    return commands.stream().map(command -> sendCommand(sagaType, sagaId, command.getDestinationChannel(), command.getResource(),
            command.getCommand(), sagaReplyChannel)).reduce( (a, b) -> b).orElse(null);

  }
}
