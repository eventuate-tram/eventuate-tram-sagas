package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.spring.id.IdGeneratorConfiguration;
import io.eventuate.common.spring.jdbc.EventuateCommonJdbcOperationsConfiguration;
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
@SpringBootTest(classes = SagaInstanceRepositoryJdbcIntegrationTest.Config.class,  webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration
public class SagaInstanceRepositoryJdbcIntegrationTest {

  private String sagaType = UUID.randomUUID().toString();
  private SagaInstance sagaInstance = new SagaInstance(sagaType, null, "SomeState", "lastRequestId", new SerializedSagaData("sagaDatType", "{}"), Collections.emptySet());

  @Configuration
  @Import({EventuateCommonJdbcOperationsConfiguration.class, IdGeneratorConfiguration.class})
  public static class Config {

    @Bean
    public SagaInstanceRepository sagaInstanceRepository(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                                         IdGenerator idGenerator,
                                                         EventuateSchema eventuateSchema) {
      return new SagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor, idGenerator, eventuateSchema);
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

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionWhenInstanceNotFound() {
    sagaInstanceRepository.find(sagaType, "unknown");
  }
}