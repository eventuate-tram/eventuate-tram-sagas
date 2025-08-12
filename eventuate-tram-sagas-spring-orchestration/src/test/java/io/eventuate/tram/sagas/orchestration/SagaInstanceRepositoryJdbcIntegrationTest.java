package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SagaInstanceRepositoryJdbcIntegrationTest.Config.class,  webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration
public class SagaInstanceRepositoryJdbcIntegrationTest {

  private String sagaType = UUID.randomUUID().toString();
  private SagaInstance sagaInstance = new SagaInstance(sagaType, null, "SomeState", "lastRequestId", new SerializedSagaData("sagaDatType", "{}"), Collections.emptySet());

  @Configuration
  @Import(EventuateCommonJdbcOperationsConfiguration.class)
  public static class Config {

    @Bean
    public SagaInstanceRepository sagaInstanceRepository(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                         EventuateSchema eventuateSchema) {
      return new SagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor, new ApplicationIdGenerator(), eventuateSchema);
    }


  }

  @Autowired
  private SagaInstanceRepository sagaInstanceRepository;

  @Test
  public void shouldSaveAndLoad() {
    assertNotNull(sagaInstance.getStateName());
    sagaInstanceRepository.save(sagaInstance);
    SagaInstance result = sagaInstanceRepository.find(sagaType, sagaInstance.getId());
    assertNotNull(result);
    assertEquals(sagaInstance.getStateName(), result.getStateName());
  }

  @Test
  public void shouldThrowExceptionWhenInstanceNotFound() {
    assertThrows(RuntimeException.class, () ->
      sagaInstanceRepository.find(sagaType, "unknown"));
  }
}