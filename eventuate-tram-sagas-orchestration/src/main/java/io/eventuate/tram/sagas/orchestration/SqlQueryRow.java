package io.eventuate.tram.sagas.orchestration;

public interface SqlQueryRow {
    String getString(String name);
    boolean getBoolean(String name);
}
