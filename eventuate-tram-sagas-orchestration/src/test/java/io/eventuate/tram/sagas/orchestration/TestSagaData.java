package io.eventuate.tram.sagas.orchestration;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TestSagaData {
  private String label;

  public TestSagaData() {
  }

  public TestSagaData(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    TestSagaData that = (TestSagaData) o;

    return new EqualsBuilder()
            .append(label, that.label)
            .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
            .append(label)
            .toHashCode();
  }
}
