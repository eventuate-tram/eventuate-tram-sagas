package io.eventuate.tram.sagas.simpledsl;

import java.util.function.BiConsumer;

public class LocalExceptionSaver<Data> {
    private final Class<?> exceptionType;
    private final BiConsumer<Data, RuntimeException> exceptionConsumer;

    public  LocalExceptionSaver(Class<?> exceptionType, BiConsumer<Data,RuntimeException> exceptionConsumer) {
        this.exceptionType = exceptionType;
        this.exceptionConsumer = exceptionConsumer;
    }

    public boolean shouldSave(Exception e) {
        return exceptionType.isInstance(e);
    }

    public void save(Data data, RuntimeException e) {
        exceptionConsumer.accept(data, e);
    }
}
