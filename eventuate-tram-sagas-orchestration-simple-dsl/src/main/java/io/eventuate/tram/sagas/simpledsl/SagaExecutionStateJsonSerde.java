package io.eventuate.tram.sagas.simpledsl;

import io.eventuate.common.json.mapper.JSonMapper;

public class SagaExecutionStateJsonSerde {
  public static SagaExecutionState decodeState(String currentState) {
    return JSonMapper.fromJson(currentState, SagaExecutionState.class);
  }

  public static String encodeState(SagaExecutionState state) {
    return JSonMapper.toJson(state);
  }
}
