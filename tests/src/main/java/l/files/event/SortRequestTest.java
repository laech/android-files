package l.files.event;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;

import static l.files.setting.SortBy.DATE_MODIFIED;
import static l.files.setting.SortBy.NAME;

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

