package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.producer.CommandProducer;
import io.eventuate.tram.sagas.common.SagaCommandHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class SagaCommandProducer {

  @Autowired
  private CommandProducer commandProducer;

  public SagaCommandProducer() {
  }

  public SagaCommandProducer(CommandProducer commandProducer) {
    this.commandProducer = commandProducer;
  }



  public void sendCommand(String sagaType, String sagaId, String destinationChannel, String resource, String requestId, Command command, String replyTo) {
    Map<String, String> headers = new HashMap<>();
    headers.put(SagaCommandHeaders.SAGA_TYPE, sagaType);
    headers.put(SagaCommandHeaders.SAGA_ID, sagaId);
    headers.put(SagaCommandHeaders.SAGA_REQUEST_ID, requestId);
    commandProducer.send(destinationChannel, resource, command, replyTo, headers);
  }
}
