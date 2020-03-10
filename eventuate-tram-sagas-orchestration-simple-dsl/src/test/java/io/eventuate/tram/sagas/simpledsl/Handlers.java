package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.tram.commands.common.Failure;
import io.eventuate.tram.commands.common.Success;

public interface Handlers {
  void success1(ConditionalSagaData data, Success m);
  void failure1(ConditionalSagaData data, Failure m);
  void compensating1(ConditionalSagaData data, Success m);

  void success2(ConditionalSagaData data, Success m);
  void failure2(ConditionalSagaData data, Failure m);
}


