package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.common.id.ApplicationIdGenerator;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.tram.sagas.common.SagaInstanceRepositorySql;
import io.eventuate.tram.sagas.orchestration.DestinationAndResource;
import io.eventuate.tram.sagas.orchestration.SagaInstance;
import org.junit.Test;
import org.mockito.InOrder;
import reactor.core.publisher.Flux;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReactiveSagaInstanceRepositoryJdbcTest {

  @Test
  public void testFindUsesExpectedQueriesInCorrectOrder() {
    EventuateSchema eventuateSchema = new EventuateSchema();
    SagaInstanceRepositorySql sagaInstanceRepositorySql =
            new SagaInstanceRepositorySql(eventuateSchema);

    EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor =
            mock(EventuateReactiveJdbcStatementExecutor.class);

    when(eventuateJdbcStatementExecutor.queryForList(eq(sagaInstanceRepositorySql.getSelectFromSagaInstanceParticipantsSql()), any(), eq("sagaId"), eq("sagaType")))
            .thenReturn(Flux.just(new DestinationAndResource("destination", "resource")));

    when(eventuateJdbcStatementExecutor.queryForList(eq(sagaInstanceRepositorySql.getSelectFromSagaInstanceSql()), any(), eq("sagaType"), eq("sagaId")))
            .thenReturn(Flux.just(new SagaInstance("sagaType", "sagaId", "stateName", "lastRequestId", null, Collections.singleton(new DestinationAndResource("destination", "resource")))));

    ReactiveSagaInstanceRepository sagaInstanceRepository =
            new ReactiveSagaInstanceRepositoryJdbc(eventuateJdbcStatementExecutor, new ApplicationIdGenerator(), eventuateSchema);

    sagaInstanceRepository.find("sagaType", "sagaId").block();

    InOrder inOrder = inOrder(eventuateJdbcStatementExecutor);

    inOrder.verify(eventuateJdbcStatementExecutor).queryForList(eq(sagaInstanceRepositorySql.getSelectFromSagaInstanceParticipantsSql()), any(), eq("sagaId"), eq("sagaType"));
    inOrder.verify(eventuateJdbcStatementExecutor).queryForList(eq(sagaInstanceRepositorySql.getSelectFromSagaInstanceSql()), any(), eq("sagaType"), eq("sagaId"));
  }
}
