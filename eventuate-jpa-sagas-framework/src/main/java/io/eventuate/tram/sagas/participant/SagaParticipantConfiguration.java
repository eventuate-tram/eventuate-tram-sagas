package io.eventuate.tram.sagas.participant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaParticipantConfiguration {

  @Bean
  public SagaLockManager sagaLockManager() {
    return new SagaLockManagerImpl();
  }
}
