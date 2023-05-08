package io.eventuate.tram.sagas.eventsourcingsupport;

import io.eventuate.DispatchedEvent;
import io.eventuate.Event;
import io.eventuate.SubscriberOptions;
import io.eventuate.sync.EventuateAggregateStore;
import io.eventuate.tram.commands.common.CommandMessageHeaders;
import io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toMap;

public class SagaReplyRequestedEventSubscriber {

  @Autowired
  private MessageProducer messageProducer;

  @Autowired
  private EventuateAggregateStore aggregateStore;
  private String subscriberId;
  private Set<String> aggregateTypes;

  public SagaReplyRequestedEventSubscriber(String subscriberId, Set<String> aggregateTypes) {
    this.subscriberId = subscriberId;
    this.aggregateTypes = aggregateTypes;
  }

  @PostConstruct
  public void subscribe() {
    Map<String, Set<String>> aggregatesAndEvents =
            aggregateTypes.stream().collect(toMap(Function.identity(), x -> singleton(SagaReplyRequestedEvent.class.getName())));

    aggregateStore.subscribe(subscriberId,
            aggregatesAndEvents,
            SubscriberOptions.DEFAULTS,
            this::sendReply);
  }

  public CompletableFuture<Object> sendReply(DispatchedEvent<Event> de) {
    SagaReplyRequestedEvent event = (SagaReplyRequestedEvent) de.getEvent();
    Message reply = CommandHandlerReplyBuilder.withSuccess();
    messageProducer.send(event.getCorrelationHeaders().get(CommandMessageHeaders.inReply(CommandMessageHeaders.REPLY_TO)),
            MessageBuilder
                    .withMessage(reply)
                    .withExtraHeaders("", event.getCorrelationHeaders())
                    .build());
    return CompletableFuture.completedFuture(null);
  }
}
