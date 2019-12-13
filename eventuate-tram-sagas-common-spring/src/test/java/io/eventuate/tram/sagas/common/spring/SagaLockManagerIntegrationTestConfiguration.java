package io.eventuate.tram.sagas.common.spring;

import io.eventuate.tram.sagas.inmemory.TramSagaInMemoryConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@Import({TramSagaInMemoryConfiguration.class, EventuateTramSagaCommonConfiguration.class})
public class SagaLockManagerIntegrationTestConfiguration {
}
