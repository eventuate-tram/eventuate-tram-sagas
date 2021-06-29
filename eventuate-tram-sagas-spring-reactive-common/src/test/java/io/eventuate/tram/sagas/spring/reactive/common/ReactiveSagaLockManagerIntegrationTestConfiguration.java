package io.eventuate.tram.sagas.spring.reactive.common;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import(EventuateReactiveTramSagaCommonConfiguration.class)
public class ReactiveSagaLockManagerIntegrationTestConfiguration {
}
