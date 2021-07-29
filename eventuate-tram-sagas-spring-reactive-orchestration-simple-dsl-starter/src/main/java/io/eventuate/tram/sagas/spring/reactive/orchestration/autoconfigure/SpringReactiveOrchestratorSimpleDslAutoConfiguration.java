package io.eventuate.tram.sagas.spring.reactive.orchestration.autoconfigure;

import io.eventuate.tram.sagas.spring.reactive.orchestration.ReactiveSagaOrchestratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ReactiveSagaOrchestratorConfiguration.class)
public class SpringReactiveOrchestratorSimpleDslAutoConfiguration {
}
