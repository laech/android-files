package l.files;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import l.files.event.RemoveBookmarkRequest;

import java.io.File;

public final class RemoveBookmarkRequestTest extends TestCase {

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new RemoveBookmarkRequest(new File("/")),
            new RemoveBookmarkRequest(new File("/")))
        .addEqualityGroup(
            new RemoveBookmarkRequest(new File("abc")),
            new RemoveBookmarkRequest(new File("abc")))
        .testEquals();
  }

}
