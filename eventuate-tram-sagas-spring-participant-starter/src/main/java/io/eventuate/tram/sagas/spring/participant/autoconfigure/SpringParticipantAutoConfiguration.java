package io.eventuate.tram.sagas.spring.participant.autoconfigure;


import io.eventuate.tram.sagas.spring.participant.SagaParticipantConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SagaParticipantConfiguration.class)
public class SpringParticipantAutoConfiguration {
}
