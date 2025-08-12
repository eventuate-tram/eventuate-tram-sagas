package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.tram.sagas.orchestration.DestinationAndResource;
import io.eventuate.tram.sagas.orchestration.SagaInstance;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepositorySql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ReactiveSagaInstanceRepositoryJdbc implements ReactiveSagaInstanceRepository {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor;
  private IdGenerator idGenerator;

  private SagaInstanceRepositorySql sagaInstanceRepositorySql;

  public ReactiveSagaInstanceRepositoryJdbc(EventuateReactiveJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                            IdGenerator idGenerator,
                                            EventuateSchema eventuateSchema) {
    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;
    this.idGenerator = idGenerator;

    sagaInstanceRepositorySql = new SagaInstanceRepositorySql(eventuateSchema);
  }

  @Override
  public Mono<Void> save(SagaInstance sagaInstance) {
    sagaInstance.setId(idGenerator.genIdAsString());

    logger.info("Saving {} {}", sagaInstance.getSagaType(), sagaInstance.getId());

    return  eventuateJdbcStatementExecutor
            .update(sagaInstanceRepositorySql.getInsertIntoSagaInstanceSql(),
                    sagaInstanceRepositorySql.makeSaveArgs(sagaInstance))
            .then(Mono.defer(() -> saveDestinationsAndResources(sagaInstance)));
  }

  private Mono<Void> saveDestinationsAndResources(SagaInstance sagaInstance) {

    List<Mono<Long>> result = new ArrayList<>();

    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
        Mono<Long> update = eventuateJdbcStatementExecutor.update(sagaInstanceRepositorySql.getInsertIntoSagaInstanceParticipantsSql(),
                sagaInstance.getSagaType(),
                sagaInstance.getId(),
                dr.getDestination(),
                dr.getResource()
        ).onErrorResume(EventuateDuplicateKeyException.class, e -> {
          logger.info("key duplicate: sagaType = {}, sagaId = {}, destination = {}, resource = {}",
                  sagaInstance.getSagaType(),
                  sagaInstance.getId(),
                  dr.getDestination(),
                  dr.getResource());

          return Mono.just(0L);
        });

        result.add(update);
    }

    return Flux.mergeSequential(result).then();
  }


  @Override
  public Mono<SagaInstance> find(String sagaType, String sagaId) {
    Flux<DestinationAndResource> destinationAndResources = eventuateJdbcStatementExecutor.queryForList(sagaInstanceRepositorySql.getSelectFromSagaInstanceParticipantsSql(),
            (row, rowMetadata) -> new DestinationAndResource(row.get("destination").toString(), row.get("resource").toString()),
            sagaType, sagaId);

    return destinationAndResources.collectList().flatMap(dar ->
      eventuateJdbcStatementExecutor.queryForList(sagaInstanceRepositorySql.getSelectFromSagaInstanceSql(),
              (row, rowMetadata) ->
                      sagaInstanceRepositorySql.mapToSagaInstance(sagaType, sagaId, new HashSet<>(dar), new ReactiveSqlQueryRow(row)),
              sagaType, sagaId).single());
  }


  @Override
  public Mono<Void> update(SagaInstance sagaInstance) {
    return eventuateJdbcStatementExecutor
            .update(sagaInstanceRepositorySql.getUpdateSagaInstanceSql(), sagaInstanceRepositorySql.makeUpdateArgs(sagaInstance))
            .flatMap(count -> {
              if (count != 1) {
                return Mono.error(new RuntimeException("Should be 1 : " + count));
              }
              else return Mono.empty();
            }).then(Mono.defer(() -> saveDestinationsAndResources(sagaInstance)));
  }
}
