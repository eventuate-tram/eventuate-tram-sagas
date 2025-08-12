package io.eventuate.tram.sagas.spring.participant.autoconfigure;


import io.eventuate.tram.sagas.spring.participant.SagaParticipantConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(SagaParticipantConfiguration.class)
public class SpringParticipantAutoConfiguration {
}
