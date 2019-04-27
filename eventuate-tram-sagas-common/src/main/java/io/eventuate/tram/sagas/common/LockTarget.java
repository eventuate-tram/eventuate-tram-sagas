package io.eventuate.tram.sagas.common;

public class LockTarget {
  private String target;

  public LockTarget(Class targetClass, Object targetId) {
    this(targetClass.getName(), targetId.toString());
  }


  public LockTarget(String targetClass, String targetId) {
    this(targetClass + "/" + targetId);
  }

  public LockTarget(String target) {
    this.target = target;
  }

  public String getTarget() {
    return target;
  }
}
