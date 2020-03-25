package io.eventuate.tram.sagas.spring.orchestration.autoconfigure;

import io.eventuate.tram.sagas.spring.orchestration.SagaOrchestratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SagaOrchestratorConfiguration.class)
public class SpringOrchestratorSimpleDslAutoConfigure {
}
