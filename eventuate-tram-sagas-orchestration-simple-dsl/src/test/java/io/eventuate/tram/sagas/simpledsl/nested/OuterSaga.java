package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;

public class OuterSaga implements SimpleSaga<OuterSagaData>  {
    private final SagaDefinition<OuterSagaData> sagaDefinition;

    public OuterSaga() {
        this.sagaDefinition = step()
                .invokeParticipant(OuterSagaData::reserveCredit)
                .withCompensation(OuterSagaData::releaseCredit)
            .step()
               .invokeLocal(OuterSagaData::approveOrder)
            .build()
         ;
    }


    @Override
    public SagaDefinition<OuterSagaData> getSagaDefinition() {
        return this.sagaDefinition;
    }

    @Override
    public void onSagaCompletedSuccessfully(String sagaId, OuterSagaData sagaData) {
        SimpleSaga.super.onSagaCompletedSuccessfully(sagaId, sagaData);
    }
}
