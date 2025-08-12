package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.id.IdGenerator;
import io.eventuate.common.jdbc.EventuateDuplicateKeyException;
import io.eventuate.common.jdbc.EventuateJdbcStatementExecutor;
import io.eventuate.common.jdbc.EventuateSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SagaInstanceRepositoryJdbc implements SagaInstanceRepository {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor;
  private IdGenerator idGenerator;

  private SagaInstanceRepositorySql sagaInstanceRepositorySql;

  public SagaInstanceRepositoryJdbc(EventuateJdbcStatementExecutor eventuateJdbcStatementExecutor,
                                    IdGenerator idGenerator,
                                    EventuateSchema eventuateSchema) {
    this.eventuateJdbcStatementExecutor = eventuateJdbcStatementExecutor;
    this.idGenerator = idGenerator;

    sagaInstanceRepositorySql = new SagaInstanceRepositorySql(eventuateSchema);
  }

  @Override
  public void save(SagaInstance sagaInstance) {
    sagaInstance.setId(idGenerator.genIdAsString());
    logger.info("Saving {} {}", sagaInstance.getSagaType(), sagaInstance.getId());

    eventuateJdbcStatementExecutor.update(sagaInstanceRepositorySql.getInsertIntoSagaInstanceSql(),
            sagaInstanceRepositorySql.makeSaveArgs(sagaInstance));

    saveDestinationsAndResources(sagaInstance);
  }

  private void saveDestinationsAndResources(SagaInstance sagaInstance) {
    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
      try {
        eventuateJdbcStatementExecutor.update(sagaInstanceRepositorySql.getInsertIntoSagaInstanceParticipantsSql(),
                sagaInstance.getSagaType(),
                sagaInstance.getId(),
                dr.getDestination(),
                dr.getResource()
        );
      } catch (EventuateDuplicateKeyException e) {
        logger.info("key duplicate: sagaType = {}, sagaId = {}, destination = {}, resource = {}",
                sagaInstance.getSagaType(),
                sagaInstance.getId(),
                dr.getDestination(),
                dr.getResource());
      }
    }
  }

  @Override
  public SagaInstance find(String sagaType, String sagaId) {
    logger.info("finding {} {}", sagaType, sagaId);

    Set<DestinationAndResource> destinationsAndResources = new HashSet<>(eventuateJdbcStatementExecutor.query(
            sagaInstanceRepositorySql.getSelectFromSagaInstanceParticipantsSql(),
            (rs, rownum) ->
                    new DestinationAndResource(rs.getString("destination"), rs.getString("resource")),
            sagaType,
            sagaId));

    return eventuateJdbcStatementExecutor.query(
            sagaInstanceRepositorySql.getSelectFromSagaInstanceSql(),
            (rs, rownum) -> sagaInstanceRepositorySql.mapToSagaInstance(sagaType, sagaId, destinationsAndResources, new JdbcSqlQueryRow(rs)),
            sagaType,
            sagaId).stream().findFirst().orElseThrow( () -> new RuntimeException("Cannot find saga instance %s %s".formatted(sagaType, sagaId)));
  }

  @Override
  public void update(SagaInstance sagaInstance) {
    logger.info("Updating {} {}", sagaInstance.getSagaType(), sagaInstance.getId());
    int count = eventuateJdbcStatementExecutor.update(sagaInstanceRepositorySql.getUpdateSagaInstanceSql(),
            sagaInstanceRepositorySql.makeUpdateArgs(sagaInstance));

    if (count != 1) {
      throw new RuntimeException("Should be 1 : " + count);
    }

    saveDestinationsAndResources(sagaInstance);
  }

}
