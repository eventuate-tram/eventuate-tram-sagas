package io.eventuate.tram.sagas.spring.testing.contract;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.spring.id.IdGeneratorConfiguration;
import io.eventuate.tram.sagas.orchestration.SagaCommandProducer;
import io.eventuate.tram.sagas.spring.orchestration.SagaOrchestratorConfiguration;
import io.eventuate.tram.spring.cloudcontractsupport.EventuateContractVerifierConfiguration;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({IdGeneratorConfiguration.class, SagaOrchestratorConfiguration.class, EventuateContractVerifierConfiguration.class})
public class EventuateTramSagasSpringCloudContractSupportConfiguration {

  @Bean
  public SagaMessagingTestHelper sagaMessagingTestHelper(ContractVerifierMessaging contractVerifierMessaging, SagaCommandProducer sagaCommandProducer, IdGenerator idGenerator) {
    return new SagaMessagingTestHelper(contractVerifierMessaging, sagaCommandProducer, idGenerator);
  }

}
