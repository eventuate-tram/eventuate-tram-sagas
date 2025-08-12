package io.eventuate.tram.sagas.spring.reactive.orchestration.autoconfigure;

import io.eventuate.tram.sagas.spring.reactive.orchestration.ReactiveSagaOrchestratorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ReactiveSagaOrchestratorConfiguration.class)
public class SpringReactiveOrchestratorSimpleDslAutoConfiguration {
}
