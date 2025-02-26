package io.eventuate.tram.sagas.simpledsl.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SagaParticipantOperation {

  Class<?> commandClass() default Object.class;
  Class<?>[] replyClasses() default {};

}
