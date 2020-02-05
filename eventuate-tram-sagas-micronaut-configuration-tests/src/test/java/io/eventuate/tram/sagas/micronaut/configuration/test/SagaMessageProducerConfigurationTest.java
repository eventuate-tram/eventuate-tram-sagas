package io.eventuate.tram.sagas.micronaut.configuration.test;

import io.eventuate.tram.messaging.producer.common.MessageProducerImplementation;
import io.eventuate.tram.messaging.producer.jdbc.MessageProducerJdbcImpl;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@MicronautTest(transactional = false)
public class SagaMessageProducerConfigurationTest {

  @Inject
  private MessageProducerImplementation messageProducer;

  @Test
  public void testThatJdbcProducerIsUsed() {
    Assert.assertTrue(messageProducer instanceof MessageProducerJdbcImpl);
  }
}