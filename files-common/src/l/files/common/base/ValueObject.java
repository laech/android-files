package l.files.common.base;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

import org.apache.commons.lang3.builder.StandardToStringStyle;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ValueObject {

  private static final ToStringStyle STYLE;

  static {
    StandardToStringStyle style = new StandardToStringStyle();
    style.setUseFieldNames(false);
    style.setUseShortClassName(true);
    style.setUseIdentityHashCode(false);
    style.setContentStart("{");
    style.setContentEnd("}");
    STYLE = style;
  }

  @Override public final int hashCode() {
    return reflectionHashCode(this);
  }

  @Override public final boolean equals(Object o) {
    return reflectionEquals(this, o);
  }

  @Override public final String toString() {
    return reflectionToString(this, STYLE);
  }
}
