package io.eventuate.tram.sagas.participant;

import io.eventuate.tram.messaging.common.MessageImpl;
import io.eventuate.tram.sagas.common.LockTarget;

import java.util.Map;
import java.util.Optional;

public class SagaReplyMessage extends MessageImpl {
  private Optional<LockTarget> lockTarget;

  public SagaReplyMessage(String body, Map<String, String> headers, Optional<LockTarget> lockTarget) {
    super(body, headers);
    this.lockTarget = lockTarget;
  }

  public Optional<LockTarget> getLockTarget() {
    return lockTarget;
  }

  public boolean hasLockTarget() {
    return lockTarget.isPresent();
  }
}
