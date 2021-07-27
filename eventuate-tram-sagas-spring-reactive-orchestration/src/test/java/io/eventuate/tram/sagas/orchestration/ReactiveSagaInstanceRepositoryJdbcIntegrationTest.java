package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.common.spring.jdbc.reactive.EventuateCommonReactiveDatabaseConfiguration;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceRepository;
import io.eventuate.tram.sagas.reactive.orchestration.ReactiveSagaInstanceRepositoryJdbc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveSagaInstanceRepositoryJdbcIntegrationTest.Config.class,  webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration
public class ReactiveSagaInstanceRepositoryJdbcIntegrationTest {

  private String sagaType = UUID.randomUUID().toString();
  private SagaInstance sagaInstance =
          new SagaInstance(sagaType, null, "SomeState", "lastRequestId", new SerializedSagaData("sagaDatType", "{}"), Collections.emptySet());

  @Configuration
  @Import(EventuateCommonReactiveDatabaseConfiguration.class)
  public static class Config {
    @Bean
    public ReactiveSagaInstanceRepository sagaInstanceRepository(EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                                 EventuateSchema eventuateSchema) {
      return new ReactiveSagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor, new ApplicationIdGenerator(), eventuateSchema);
    }
  }

  @Autowired
  private ReactiveSagaInstanceRepository sagaInstanceRepository;

  @Test
  public void shouldSaveLoadUpdate() {
    assertNotNull(sagaInstance.getStateName());
    sagaInstanceRepository.save(sagaInstance).block();
    SagaInstance result = sagaInstanceRepository.find(sagaType, sagaInstance.getId()).block();
    assertNotNull(result);
    assertEquals(sagaInstance.getStateName(), result.getStateName());
    sagaInstance.setStateName("UpdatedState");
    sagaInstanceRepository.update(sagaInstance).block();
    result = sagaInstanceRepository.find(sagaType, sagaInstance.getId()).block();
    assertEquals("UpdatedState", result.getStateName());
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionWhenInstanceNotFound() {
    sagaInstanceRepository.find(sagaType, "unknown").block();
  }
}