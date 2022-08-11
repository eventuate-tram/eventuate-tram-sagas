package io.eventuate.tram.sagas.reactive.simpledsl;

import reactor.core.publisher.Mono;

public interface LocalSagaSteps {

  Mono<Void> localStep1(LocalSagaData data);
  Mono<Void> localStep1Compensation(LocalSagaData data);
  Mono<Void> localStep3(LocalSagaData data);

}
