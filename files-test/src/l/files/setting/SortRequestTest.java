package l.files.setting;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;

public final class SortRequestTest extends TestCase {

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new SortRequest("x"),
            new SortRequest("x"))
        .addEqualityGroup(
            new SortRequest("y"),
            new SortRequest("y"))
        .testEquals();
  }
}

