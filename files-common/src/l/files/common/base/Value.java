package l.files.common.base;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public class Value<T> {

  private final T value;

  protected Value(T value) {
    this.value = checkNotNull(value, "value");
  }

  public final T value() {
    return value;
  }

  @Override public final int hashCode() {
    return value().hashCode();
  }

  @Override public final boolean equals(Object o) {
    return null != o
        && getClass().equals(o.getClass())
        && value().equals(((Value<?>) o).value());
  }

  @Override public final String toString() {
    return toStringHelper(this).addValue(value()).toString();
  }
}
