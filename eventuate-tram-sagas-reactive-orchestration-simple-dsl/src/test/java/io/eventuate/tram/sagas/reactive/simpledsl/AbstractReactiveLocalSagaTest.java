package io.eventuate.tram.sagas.reactive.simpledsl;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;

import static io.eventuate.tram.sagas.reactive.simpledsl.framework.ReactiveSagaUnitTestSupport.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractReactiveLocalSagaTest {
    protected LocalSagaSteps steps;

    @Before
    public void setUp() {
        steps = mock(LocalSagaSteps.class);

    }

    @Test
    public void shouldRollbackFromStep2() {
        doReturn(Mono.empty()).when(steps).localStep1(any());
        doReturn(Mono.empty()).when(steps).localStep1Compensation(any());
        doReturn(Mono.empty()).when(steps).localStep3(any());

        given().
                saga(makeSaga(), new LocalSagaData()).
                expect().
                command(new ReserveCreditCommand()).
                to("participant2").
                andGiven().
                failureReply().
                andGiven().
                expectRolledBack()
        ;
    }

    @Test
    public void shouldHandleFailureOfFirstLocalStep() {
        LocalSagaData data = new LocalSagaData();
        RuntimeException expectedCreateException = new RuntimeException("Failed local step");
        doThrow(expectedCreateException).when(steps).localStep1(data);
        given().
                saga(makeSaga(), data).
                expectException(expectedCreateException)
        ;
    }

    protected abstract SimpleReactiveSaga<LocalSagaData> makeSaga();

}
