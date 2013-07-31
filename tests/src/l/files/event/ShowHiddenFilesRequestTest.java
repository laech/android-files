package l.files.event;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;

public final class ShowHiddenFilesRequestTest extends TestCase {

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new ShowHiddenFilesRequest(true),
            new ShowHiddenFilesRequest(true))
        .addEqualityGroup(
            new ShowHiddenFilesRequest(false),
            new ShowHiddenFilesRequest(false))
        .testEquals();
  }
}
