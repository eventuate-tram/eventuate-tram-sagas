package io.eventuate.tram.sagas.simpledsl.nested;

import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.commands.consumer.CommandReplyProducer;
import io.eventuate.tram.commands.producer.CommandProducerImpl;
import io.eventuate.tram.inmemory.InMemoryMessaging;

public class InMemoryCommandProducer {

    public final CommandProducerImpl commandProducer;
    public final CommandReplyProducer commandReplyProducer;
    public final DefaultCommandNameMapping commandNameMapping;

    public InMemoryCommandProducer(CommandProducerImpl commandProducer, CommandReplyProducer commandReplyProducer, DefaultCommandNameMapping commandNameMapping) {

        this.commandProducer = commandProducer;
        this.commandReplyProducer = commandReplyProducer;
        this.commandNameMapping = commandNameMapping;
    }

    public static InMemoryCommandProducer make(InMemoryMessaging inMemoryMessaging) {
        DefaultCommandNameMapping commandNameMapping = new DefaultCommandNameMapping();
        CommandProducerImpl commandProducer = new CommandProducerImpl(inMemoryMessaging.messageProducer, commandNameMapping);
        CommandReplyProducer commandReplyProducer = new CommandReplyProducer(inMemoryMessaging.messageProducer);
        return new InMemoryCommandProducer(commandProducer, commandReplyProducer, commandNameMapping);
    }
}
