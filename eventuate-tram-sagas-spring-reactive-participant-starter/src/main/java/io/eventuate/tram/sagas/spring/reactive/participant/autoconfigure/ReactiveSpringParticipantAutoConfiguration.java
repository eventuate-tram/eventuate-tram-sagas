package io.eventuate.tram.sagas.spring.reactive.participant.autoconfigure;

import io.eventuate.tram.sagas.spring.reactive.participant.ReactiveSagaParticipantConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ReactiveSagaParticipantConfiguration.class)
public class ReactiveSpringParticipantAutoConfiguration {
}
