package io.eventuate.tram.sagas.common;

import io.eventuate.tram.commands.common.CommandMessageHeaders;

public class SagaReplyHeaders {

  public static final String REPLY_SAGA_TYPE = CommandMessageHeaders.inReply(SagaCommandHeaders.SAGA_TYPE);
  public static final String REPLY_SAGA_ID = CommandMessageHeaders.inReply(SagaCommandHeaders.SAGA_ID);
  public static final String REPLY_SAGA_REQUEST_ID = CommandMessageHeaders.inReply(SagaCommandHeaders.SAGA_REQUEST_ID);

  public static final String REPLY_LOCKED = "saga-locked-target";
}
