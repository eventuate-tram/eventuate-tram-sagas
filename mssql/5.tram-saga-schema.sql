USE eventuate;

DROP Table IF Exists eventuate.saga_instance_participants;
GO

DROP Table IF Exists eventuate.saga_instance;
GO

DROP Table IF Exists eventuate.saga_lock_table;
GO

DROP Table IF Exists eventuate.saga_stash_table;
GO

CREATE TABLE eventuate.saga_instance_participants (
  saga_type VARCHAR(255) NOT NULL,
  saga_id VARCHAR(100) NOT NULL,
  destination VARCHAR(100) NOT NULL,
  resource VARCHAR(100) NOT NULL,
  PRIMARY KEY(saga_type, saga_id, destination, resource)
);

CREATE TABLE eventuate.saga_instance(
  saga_type VARCHAR(255) NOT NULL,
  saga_id VARCHAR(100) NOT NULL,
  state_name VARCHAR(100) NOT NULL,
  last_request_id VARCHAR(100),
  end_state TINYINT,
  compensating TINYINT,
  failed TINYINT,
  saga_data_type VARCHAR(1000) NOT NULL,
  saga_data_json VARCHAR(1000) NOT NULL,
  PRIMARY KEY(saga_type, saga_id)
);

create table eventuate.saga_lock_table(
  target VARCHAR(100) PRIMARY KEY,
  saga_type VARCHAR(255) NOT NULL,
  saga_Id VARCHAR(100) NOT NULL
);

create table eventuate.saga_stash_table(
  message_id VARCHAR(100) PRIMARY KEY,
  target VARCHAR(100) NOT NULL,
  saga_type VARCHAR(255) NOT NULL,
  saga_id VARCHAR(100) NOT NULL,
  message_headers VARCHAR(1000) NOT NULL,
  message_payload VARCHAR(1000) NOT NULL
);
