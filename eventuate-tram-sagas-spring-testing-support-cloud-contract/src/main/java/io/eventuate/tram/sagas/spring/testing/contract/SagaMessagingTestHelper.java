package io.eventuate.tram.sagas.spring.testing.contract;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.consumer.CommandWithDestination;
import io.eventuate.tram.sagas.orchestration.SagaCommandProducer;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;

import java.util.Collections;

public class SagaMessagingTestHelper {

  private ContractVerifierMessaging contractVerifierMessaging;

  private SagaCommandProducer sagaCommandProducer;

  private IdGenerator idGenerator;

  public SagaMessagingTestHelper(ContractVerifierMessaging contractVerifierMessaging, SagaCommandProducer sagaCommandProducer, IdGenerator idGenerator) {
    this.contractVerifierMessaging = contractVerifierMessaging;
    this.sagaCommandProducer = sagaCommandProducer;
    this.idGenerator = idGenerator;
  }

  public <C extends Command, R> R sendAndReceiveCommand(CommandEndpoint<C> commandEndpoint, C command, Class<R> replyClass, String sagaType) {
    // TODO verify that replyClass is allowed

    String sagaId = idGenerator.genId().asString();

    String replyTo = sagaType + "-reply";
    sagaCommandProducer.sendCommands(sagaType, sagaId, Collections.singletonList(new CommandWithDestination(commandEndpoint.getCommandChannel(), (String)null, (Command)command)), replyTo);

    ContractVerifierMessage response = contractVerifierMessaging.receive(replyTo);

    String payload = (String) response.getPayload();
    return (R) JSonMapper.fromJson(payload, replyClass);
  }
}
