USE eventuate;

CREATE TABLE customer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(1024),
  version BIGINT,
  creation_time BIGINT,
  credit_limit DECIMAL
);

CREATE TABLE credit_reservation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT,
  order_id BIGINT,
  reservation DECIMAL
);

CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  state VARCHAR(32),
  rejection_reason VARCHAR(32),
  customer_id BIGINT,
  order_total DECIMAL
);