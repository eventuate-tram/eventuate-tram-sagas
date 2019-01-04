package io.eventuate.tram.sagas.orchestration;

public interface SagaManager<Data> {
    SagaInstance create(Data sagaData);

    // TODO or should the saga have a pseudo-step that locks the resource

    SagaInstance create(Data sagaData, String lockTarget);

    SagaInstance create(Data data, Class targetClass, Object targetId);
}
