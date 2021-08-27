package io.eventuate.tram.sagas.reactive.simpledsl;

public interface SimpleReactiveSagaDsl<Data> {
  default ReactiveStepBuilder<Data> step() {
    SimpleReactiveSagaDefinitionBuilder<Data> builder = new SimpleReactiveSagaDefinitionBuilder<>();
    return new ReactiveStepBuilder<>(builder);
  }
}