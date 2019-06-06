package io.eventuate.tram.sagas.participant;

import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.commands.common.Success;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.common.LockTarget;

import java.util.Optional;

public class SagaReplyMessageBuilder extends MessageBuilder {

  private Optional<LockTarget> lockTarget = Optional.empty();

  public SagaReplyMessageBuilder(LockTarget lockTarget) {
    this.lockTarget = Optional.of(lockTarget);
  }

  public static SagaReplyMessageBuilder withLock(Class type, Object id) {
    return new SagaReplyMessageBuilder(new LockTarget(type, id));
  }

  private <T> Message with(T reply, CommandReplyOutcome outcome) {
    this.body = JSonMapper.toJson(reply);
    withHeader(ReplyMessageHeaders.REPLY_OUTCOME, outcome.name());
    withHeader(ReplyMessageHeaders.REPLY_TYPE, reply.getClass().getName());
    return new SagaReplyMessage(body, headers, lockTarget);
  }

  public Message withSuccess(Object reply) {
    return with(reply, CommandReplyOutcome.SUCCESS);
  }

  public Message withSuccess() {
    return withSuccess(new Success());
  }

}
