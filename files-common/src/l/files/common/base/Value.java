package l.files.common.base;

import static com.google.common.base.Preconditions.checkNotNull;

public class Value<T> extends ValueObject {

  private final T value;

  protected Value(T value) {
    this.value = checkNotNull(value, "value");
  }

  public final T value() {
    return value;
  }
}
