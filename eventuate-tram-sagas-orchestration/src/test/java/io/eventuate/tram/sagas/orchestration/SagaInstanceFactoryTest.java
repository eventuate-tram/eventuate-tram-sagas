package io.eventuate.tram.sagas.orchestration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SagaInstanceFactoryTest {



  class SagaData {}

  private SagaManagerImpl<SagaData> sagaManager;
  private SagaInstanceFactory sagaInstanceFactory;
  private SagaManagerFactory sagaManagerFactory;

  private SagaData sagaData1;
  private SagaData sagaData2;

  private SagaInstance expectedSi1;
  private SagaInstance expectedSi2;

  private Saga<SagaData> saga;

  @BeforeEach
  public void setUp() {
    sagaManagerFactory = mock(SagaManagerFactory.class);

    sagaManager = mock(SagaManagerImpl.class);

    sagaData1 = new SagaData();
    sagaData2 = new SagaData();
    saga = mock(Saga.class);
    expectedSi1 = mock(SagaInstance.class);
    expectedSi2 = mock(SagaInstance.class);
    when(sagaManagerFactory.make(saga)).thenReturn(sagaManager);
    sagaInstanceFactory = new SagaInstanceFactory(sagaManagerFactory, Collections.singleton(saga));
  }

  @Test
  public void shouldCreateSagaInstance() {

    when(sagaManager.create(sagaData1)).thenReturn(expectedSi1);
    when(sagaManager.create(sagaData2)).thenReturn(expectedSi2);

    SagaInstance si1 = sagaInstanceFactory.create(saga, sagaData1);
    assertEquals(expectedSi1, si1);
    verify(sagaManagerFactory, times(1)).make(saga);

    SagaInstance si2 = sagaInstanceFactory.create(saga, sagaData2);
    assertEquals(expectedSi2, si2);
    verify(sagaManagerFactory, times(1)).make(saga);
  }

}