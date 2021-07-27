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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReactiveSagaInstanceRepositoryJdbcIntegrationTest.Config.class,  webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration
public class ReactiveSagaInstanceRepositoryJdbcIntegrationTest {

  private String sagaType = generateId();
  private String sagaState = generateId();
  private String lastRequestId = generateId();
  private String sagaDataType = generateId();
  private String destination = generateId();
  private String resource = generateId();

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

  private SagaInstance sagaInstance;

  @Test
  public void shouldSaveLoadUpdate() {
    sagaInstance = createSagaInstance();
    sagaInstanceRepository.save(sagaInstance).block();
    sagaInstance = sagaInstanceRepository.find(sagaType, sagaInstance.getId()).block();
    assertSavedSagaInstance();
    updateSagaInstance();
    assertUpdatedSagaInstance();
  }

  private void assertSavedSagaInstance() {
    assertEquals(sagaState, sagaInstance.getStateName());
    assertEquals(lastRequestId, sagaInstance.getLastRequestId());
    assertEquals(sagaDataType, sagaInstance.getSerializedSagaData().getSagaDataType());
    assertEquals("{}", sagaInstance.getSerializedSagaData().getSagaDataJSON());
    Set<DestinationAndResource> destinationAndResources = sagaInstance.getDestinationsAndResources();
    assertEquals(singleton(new DestinationAndResource(destination, resource)), destinationAndResources);
    DestinationAndResource destinationAndResource = destinationAndResources.stream().findAny().get();
    assertEquals(destination, destinationAndResource.getDestination());
    assertEquals(resource, destinationAndResource.getResource());
  }

  private void updateSagaInstance() {
    sagaInstance.setStateName("UpdatedState");
    sagaInstance.setLastRequestId("UpdateLastId");
    sagaInstance.setSerializedSagaData(new SerializedSagaData("UpdatedSagaType", "{\"value\" : \"updatedValue\"}"));
    sagaInstance.getDestinationsAndResources().add(new DestinationAndResource("newDestination", "newResource"));
    sagaInstanceRepository.update(sagaInstance).block();
    sagaInstance = sagaInstanceRepository.find(sagaType, sagaInstance.getId()).block();
  }

  private void assertUpdatedSagaInstance() {
    assertEquals("UpdatedState", sagaInstance.getStateName());
    assertEquals("UpdateLastId", sagaInstance.getLastRequestId());
    assertEquals("UpdatedSagaType", sagaInstance.getSerializedSagaData().getSagaDataType());
    assertEquals("{\"value\" : \"updatedValue\"}", sagaInstance.getSerializedSagaData().getSagaDataJSON());

    Set<DestinationAndResource> destinationAndResources = sagaInstance.getDestinationsAndResources();
    assertEquals(new HashSet<>(asList(new DestinationAndResource(destination, resource), new DestinationAndResource("newDestination", "newResource"))), destinationAndResources);
  }

  @Test(expected = RuntimeException.class)
  public void shouldThrowExceptionWhenInstanceNotFound() {
    sagaInstanceRepository.find(sagaType, "unknown").block();
  }

  private SagaInstance createSagaInstance() {
    return new SagaInstance(sagaType,
            null,
            sagaState,
            lastRequestId,
            new SerializedSagaData(sagaDataType, "{}"),
            singleton(new DestinationAndResource(destination, resource)));
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}