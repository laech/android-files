package l.files.app.setting;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;

import static l.files.app.setting.Sort.DATE_MODIFIED;
import static l.files.app.setting.Sort.NAME;

public final class SortRequestTest extends TestCase {

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new SortRequest(NAME),
            new SortRequest(NAME))
        .addEqualityGroup(
            new SortRequest(DATE_MODIFIED),
            new SortRequest(DATE_MODIFIED))
        .testEquals();
  }
}

