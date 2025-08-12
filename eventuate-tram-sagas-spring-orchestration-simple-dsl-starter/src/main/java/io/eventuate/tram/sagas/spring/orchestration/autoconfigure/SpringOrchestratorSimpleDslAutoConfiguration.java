package io.eventuate.tram.sagas.spring.orchestration.autoconfigure;

import io.eventuate.tram.sagas.spring.orchestration.SagaOrchestratorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(SagaOrchestratorConfiguration.class)
public class SpringOrchestratorSimpleDslAutoConfiguration {
}
