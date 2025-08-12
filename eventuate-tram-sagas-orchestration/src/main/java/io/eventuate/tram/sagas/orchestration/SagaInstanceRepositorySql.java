package io.eventuate.tram.sagas.orchestration;

import io.eventuate.common.jdbc.EventuateSchema;

import java.util.Set;

public class SagaInstanceRepositorySql {

  private String insertIntoSagaInstanceSql;
  private String insertIntoSagaInstanceParticipantsSql;
  private String selectFromSagaInstanceSql;
  private String selectFromSagaInstanceParticipantsSql;
  private String updateSagaInstanceSql;

  public SagaInstanceRepositorySql(EventuateSchema eventuateSchema) {
    String sagaInstanceTable = eventuateSchema.qualifyTable("saga_instance");
    String sagaInstanceParticipantsTable = eventuateSchema.qualifyTable("saga_instance_participants");

    insertIntoSagaInstanceSql = "INSERT INTO %s(saga_type, saga_id, state_name, last_request_id, saga_data_type, saga_data_json, end_state, compensating, failed) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)".formatted(sagaInstanceTable);
    insertIntoSagaInstanceParticipantsSql = "INSERT INTO %s(saga_type, saga_id, destination, resource) values(?,?,?,?)".formatted(sagaInstanceParticipantsTable);

    selectFromSagaInstanceSql = "SELECT * FROM %s WHERE saga_type = ? AND saga_id = ?".formatted(sagaInstanceTable);
    selectFromSagaInstanceParticipantsSql = "SELECT destination, resource FROM %s WHERE saga_type = ? AND saga_id = ?".formatted(sagaInstanceParticipantsTable);

    updateSagaInstanceSql = "UPDATE %s SET state_name = ?, last_request_id = ?, saga_data_type = ?, saga_data_json = ?, end_state = ?, compensating = ?, failed = ? where saga_type = ? AND saga_id = ?".formatted(sagaInstanceTable);
  }

  public String getInsertIntoSagaInstanceSql() {
    return insertIntoSagaInstanceSql;
  }

  public String getInsertIntoSagaInstanceParticipantsSql() {
    return insertIntoSagaInstanceParticipantsSql;
  }

  public String getSelectFromSagaInstanceSql() {
    return selectFromSagaInstanceSql;
  }

  public String getSelectFromSagaInstanceParticipantsSql() {
    return selectFromSagaInstanceParticipantsSql;
  }

  public String getUpdateSagaInstanceSql() {
    return updateSagaInstanceSql;
  }

  public Object[] makeSaveArgs(SagaInstance sagaInstance) {
    return new Object[]{sagaInstance.getSagaType(),
            sagaInstance.getId(),
            sagaInstance.getStateName(),
            sagaInstance.getLastRequestId(),
            sagaInstance.getSerializedSagaData().getSagaDataType(),
            sagaInstance.getSerializedSagaData().getSagaDataJSON(),
            sagaInstance.isEndState(),
            sagaInstance.isCompensating(),
            sagaInstance.isFailed()};
  }

  public Object[] makeUpdateArgs(SagaInstance sagaInstance) {
    return new Object[]{sagaInstance.getStateName(),
            sagaInstance.getLastRequestId(),
            sagaInstance.getSerializedSagaData().getSagaDataType(),
            sagaInstance.getSerializedSagaData().getSagaDataJSON(),
            sagaInstance.isEndState(), sagaInstance.isCompensating(), sagaInstance.isFailed(),
            sagaInstance.getSagaType(), sagaInstance.getId()};
  }

  public SagaInstance mapToSagaInstance(String sagaType, String sagaId, Set<DestinationAndResource> destinationsAndResources, SqlQueryRow rs) {
    return new SagaInstance(sagaType, sagaId, rs.getString("state_name"),
            rs.getString("last_request_id"),
            new SerializedSagaData(rs.getString("saga_data_type"), rs.getString("saga_data_json")),
            destinationsAndResources,
            rs.getBoolean("end_state"),
            rs.getBoolean("compensating"),
            rs.getBoolean("failed")
    );
  }
}
