package l.files.common.base;

import static java.lang.Boolean.TRUE;

import junit.framework.TestCase;

public final class ValueTest extends TestCase {

  public void testEquals_toExtractClass() {
    Value<Boolean> subclass1 = new Value<Boolean>(true) {};
    Value<Boolean> subclass2 = new Value<Boolean>(true) {};
    assertFalse(subclass1.equals(subclass2));
  }

  public void testEquals_toSelf() {
    Value<Boolean> value = new Value<Boolean>(true) {};
    assertTrue(value.equals(value));
  }

  public void testEquals_noToNull() {
    Value<Boolean> value = new Value<Boolean>(true) {};
    assertFalse(value.equals(null));
  }

  public void testHashCode_hashesValue() {
    Value<Boolean> value = new Value<Boolean>(true) {};
    assertEquals(TRUE.hashCode(), value.hashCode());
  }
}
