package io.eventuate.tram.sagas.orchestration;

import io.eventuate.javaclient.spring.jdbc.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

public class SagaInstanceRepositoryJdbc implements SagaInstanceRepository {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private IdGenerator idGenerator;

  private String sagaInstanceTable;
  private String sagaInstanceParticipantsTable;

  public SagaInstanceRepositoryJdbc() {
    sagaInstanceTable = "eventuate.saga_instance";
    sagaInstanceParticipantsTable = "eventuate.saga_instance_participants";
  }

  @Override
  public void save(SagaInstance sagaInstance) {
    sagaInstance.setId(idGenerator.genId().asString());
    logger.info("Saving {} {}", sagaInstance.getSagaType(), sagaInstance.getId());
    jdbcTemplate.update(String.format("INSERT INTO %s(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json) VALUES(?, ?, ?,?,?,?)", sagaInstanceTable),
            sagaInstance.getSagaType(),
            sagaInstance.getId(),
            sagaInstance.getStateName(),
            sagaInstance.getLastRequestId(),
            sagaInstance.getSerializedSagaData().getSagaDataType(),
            sagaInstance.getSerializedSagaData().getSagaDataJSON());

    saveDestinationsAndResources(sagaInstance);
  }

  private void saveDestinationsAndResources(SagaInstance sagaInstance) {
    for (DestinationAndResource dr : sagaInstance.getDestinationsAndResources()) {
      try {
        jdbcTemplate.update(String.format("INSERT INTO %s(saga_type, saga_id, destination, resource) values(?,?,?,?)", sagaInstanceParticipantsTable),
                sagaInstance.getSagaType(),
                sagaInstance.getId(),
                dr.getDestination(),
                dr.getResource()
        );
      } catch (DuplicateKeyException e) {
        // do nothing
      }
    }
  }

  @Override
  public SagaInstance find(String sagaType, String sagaId) {
    logger.info("finding {} {}", sagaType, sagaId);

    Set<DestinationAndResource> destinationsAndResources = new HashSet<>(jdbcTemplate.query(
            String.format("SELECT destination, resource FROM %s WHERE saga_type = ? AND saga_id = ?", sagaInstanceParticipantsTable),
            (rs, rownum) ->
                    new DestinationAndResource(rs.getString("destination"), rs.getString("resource")),
            sagaType,
            sagaId));

    return DataAccessUtils.requiredSingleResult(jdbcTemplate.query(
            String.format("SELECT * FROM %s WHERE saga_type = ? AND saga_id = ?", sagaInstanceTable),
            (rs, rownum) ->
                    new SagaInstance(sagaType, sagaId, rs.getString("state_name"),
                            rs.getString("last_request_id"),
                            new SerializedSagaData(rs.getString("saga_data_type"), rs.getString("saga_data_json")), destinationsAndResources),
            sagaType,
            sagaId));
    // TODO insert - sagaInstance.getDestinationsAndResources();
  }

  @Override
  public void update(SagaInstance sagaInstance) {
    logger.info("Updating {} {}", sagaInstance.getSagaType(), sagaInstance.getId());
    int count = jdbcTemplate.update(String.format("UPDATE %s SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ? where saga_type = ? AND saga_id = ?", sagaInstanceTable),
            sagaInstance.getStateName(),
            sagaInstance.getLastRequestId(),
            sagaInstance.getSerializedSagaData().getSagaDataType(),
            sagaInstance.getSerializedSagaData().getSagaDataJSON(),
            sagaInstance.getSagaType(), sagaInstance.getId());
    Assert.isTrue(count == 1, "Should be 1 : " + count);
    saveDestinationsAndResources(sagaInstance);
  }

  @Override
  public <Data> SagaInstanceData<Data> findWithData(String sagaType, String sagaId) {
    SagaInstance sagaInstance = find(sagaType, sagaId);
    Data sagaData = SagaDataSerde.deserializeSagaData(sagaInstance.getSerializedSagaData());
    return new SagaInstanceData<>(sagaInstance, sagaData);
  }

}
