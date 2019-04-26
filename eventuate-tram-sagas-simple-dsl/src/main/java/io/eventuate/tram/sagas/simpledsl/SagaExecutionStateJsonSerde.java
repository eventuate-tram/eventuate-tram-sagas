package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.javaclient.commonimpl.JSonMapper;

public class SagaExecutionStateJsonSerde {
  static SagaExecutionState decodeState(String currentState) {
    return JSonMapper.fromJson(currentState, SagaExecutionState.class);
  }

  public static String encodeState(SagaExecutionState state) {
    return JSonMapper.toJson(state);
  }
}
