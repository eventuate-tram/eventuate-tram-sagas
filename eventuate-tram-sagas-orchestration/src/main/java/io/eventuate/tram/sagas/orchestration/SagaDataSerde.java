package io.eventuate.tram.sagas.orchestration;


import io.eventuate.common.json.mapper.JSonMapper;

public class SagaDataSerde {

  public static <Data> SerializedSagaData serializeSagaData(Data sagaData) {
    return new SerializedSagaData(sagaData.getClass().getName(), JSonMapper.toJson(sagaData));
  }

  public static <Data> Data deserializeSagaData(SerializedSagaData serializedSagaData) {
    Class<?> clasz = null;
    try {
      clasz = SagaDataSerde.class.getClassLoader().loadClass(serializedSagaData.getSagaDataType());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    Object x = JSonMapper.fromJson(serializedSagaData.getSagaDataJSON(), clasz);
    return (Data)x;
  }
}
