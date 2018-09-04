package io.eventuate.tram.sagas.testing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SagaParticipantChannels {
  
  private Set<String> channels;

  public SagaParticipantChannels(String... channels) {
    this.channels = new HashSet<>(Arrays.asList(channels));
  }

  public Set<String> getChannels() {
    return channels;
  }
}
