package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.sagas.orchestration.SagaInstance;
import io.eventuate.tram.sagas.orchestration.SagaInstanceRepository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySagaInstanceRepository implements SagaInstanceRepository {

    private ConcurrentHashMap<String, SagaInstance> sagaInstances = new ConcurrentHashMap<>();

    @Override
    public void save(SagaInstance sagaInstance) {
        String id = UUID.randomUUID().toString();
        sagaInstance.setId(id);
        sagaInstances.put(id, sagaInstance);
    }

    @Override
    public SagaInstance find(String sagaType, String sagaId) {
        return sagaInstances.get(sagaId);
    }

    @Override
    public void update(SagaInstance sagaInstance) {
        sagaInstances.put(sagaInstance.getId(), sagaInstance);
    }
}
