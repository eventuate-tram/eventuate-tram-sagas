package io.eventuate.tram.sagas.spring.orchestration.autoconfigure;

import io.eventuate.tram.sagas.spring.orchestration.SagaOrchestratorConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SagaOrchestratorConfiguration.class)
@ConditionalOnClass(SagaOrchestratorConfiguration.class)
public class SpringOrchestratorSimpleDslAutoConfiguration {
}
