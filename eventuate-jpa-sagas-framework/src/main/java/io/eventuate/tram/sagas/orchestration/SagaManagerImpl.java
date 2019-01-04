package io.eventuate.tram.sagas.orchestration;

import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.common.LockTarget;
import io.eventuate.tram.sagas.common.SagaReplyHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

public class SagaManagerImpl<Data> implements SagaManager<Data>, InitializingBean {

    private final Saga<Data> saga;
    private final SagaOrchestrator sagaOrchestrator;

    public SagaManagerImpl(final Saga<Data> saga, final SagaOrchestrator orchestrator) {
        this.saga = saga;
        this.sagaOrchestrator = orchestrator;
    }

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(saga.getSagaDefinition(), "Saga definition is mandatory");
        Objects.requireNonNull(saga.getSagaType(), "Saga type is mandatory");
        sagaOrchestrator.subscribe(saga.getSagaType(), this::handleMessage);
    }

    @Override
    public SagaInstance create(final Data sagaData) {
        return create(sagaData, null);
    }

    @Override
    public SagaInstance create(final Data data, final Class targetClass, Object targetId) {
        return create(data, new LockTarget(targetClass, targetId).getTarget());
    }

    @Override
    public SagaInstance create(final Data sagaData, final String resource) {
        return sagaOrchestrator.registerSaga(saga, sagaData, resource);
    }

    private void handleMessage(final Message message) {
        if (isReplyForThisSagaType(message)) {
            sagaOrchestrator.handleMessage(message);
        }
    }

    private Boolean isReplyForThisSagaType(final Message message) {
        return message.getHeader(SagaReplyHeaders.REPLY_SAGA_TYPE).map(x -> x.equals(saga.getSagaType())).orElse(false);
    }

}
