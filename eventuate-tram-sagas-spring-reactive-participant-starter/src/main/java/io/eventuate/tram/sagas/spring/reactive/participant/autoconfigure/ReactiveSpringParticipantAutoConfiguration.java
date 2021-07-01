package io.eventuate.tram.sagas.spring.reactive.participant.autoconfigure;

import io.eventuate.tram.sagas.spring.reactive.participant.ReactiveSagaParticipantConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ReactiveSagaParticipantConfiguration.class)
public class ReactiveSpringParticipantAutoConfiguration {
}
