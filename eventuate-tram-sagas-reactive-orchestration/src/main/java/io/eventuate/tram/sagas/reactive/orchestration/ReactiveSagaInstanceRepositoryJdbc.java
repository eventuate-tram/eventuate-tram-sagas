package io.eventuate.tram.sagas.reactive.orchestration;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.common.reactive.jdbc.EventuateReactiveJdbcStatementExecutor;
import io.eventuate.tram.sagas.common.SagaInstanceRepositorySql;
import io.eventuate.tram.sagas.orchestration.DestinationAndResource;
import io.eventuate.tram.sagas.orchestration.SagaInstance;
import io.eventuate.tram.sagas.orchestration.SerializedSagaData;
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
    sagaInstance.setId(idGenerator.genId(null).asString());

    logger.info("Saving {} {}", sagaInstance.getSagaType(), sagaInstance.getId());

    return  eventuateJdbcStatementExecutor
            .update(sagaInstanceRepositorySql.getInsertIntoSagaInstanceSql(),
                    sagaInstance.getSagaType(),
                    sagaInstance.getId(),
                    sagaInstance.getStateName(),
                    sagaInstance.getLastRequestId(),
                    sagaInstance.getSerializedSagaData().getSagaDataType(),
                    sagaInstance.getSerializedSagaData().getSagaDataJSON(),
                    sagaInstance.isEndState(),
                    sagaInstance.isCompensating())
            .then(Mono.defer(() -> saveDestinationsAndResources(sagaInstance)));
  }

  private Mono<Void> saveDestinationsAndResources(SagaInstance sagaInstance) {

    List<Mono<Integer>> result = new ArrayList<>();

    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
        Mono<Integer> update = eventuateJdbcStatementExecutor.update(sagaInstanceRepositorySql.getInsertIntoSagaInstanceParticipantsSql(),
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

          return Mono.just(0);
        });

        result.add(update);
    }

    return Flux.mergeSequential(result).then();
  }


  @Override
  public Mono<SagaInstance> find(String sagaType, String sagaId) {
    Flux<DestinationAndResource> destinationAndResources = eventuateJdbcStatementExecutor.queryForList(sagaInstanceRepositorySql.getSelectFromSagaInstanceSql(),
            (row, rowMetadata) -> new DestinationAndResource(row.get("destination").toString(), row.get("resource").toString()),
            sagaId, sagaType);


    Mono<List<SagaInstance>> result = destinationAndResources.collectList().flatMap(dar ->
      eventuateJdbcStatementExecutor.queryForList(sagaInstanceRepositorySql.getSelectFromSagaInstanceSql(),
              (row, rowMetadata) ->
                new SagaInstance(sagaType, sagaId, row.get("state_name").toString(),
                        row.get("last_request_id").toString(),
                        new SerializedSagaData(row.get("saga_data_type").toString(), row.get("saga_data_json").toString()), new HashSet<>(dar)),
              sagaType, sagaId).collectList());

    return result.flatMap(sagaInstances -> {
      if (sagaInstances.isEmpty()) return Mono.error(new RuntimeException(String.format("Cannot find saga instance %s %s", sagaType, sagaId)));
      else return Mono.just(sagaInstances.get(0));
    });
  }


  @Override
  public Mono<Void> update(SagaInstance sagaInstance) {
    return eventuateJdbcStatementExecutor
            .update(sagaInstanceRepositorySql.getUpdateSagaInstanceSql(),
                    sagaInstance.getStateName(),
                    sagaInstance.getLastRequestId(),
                    sagaInstance.getSerializedSagaData().getSagaDataType(),
                    sagaInstance.getSerializedSagaData().getSagaDataJSON(),
                    sagaInstance.isEndState(), sagaInstance.isCompensating(),
                    sagaInstance.getSagaType(), sagaInstance.getId())
            .flatMap(count -> {
              if (count != 1) {
                return Mono.error(new RuntimeException("Should be 1 : " + count));
              }
              else return Mono.empty();
            }).then(saveDestinationsAndResources(sagaInstance));
  }
}
