package l.files;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import l.files.event.AddBookmarkRequest;

import java.io.File;

public final class AddBookmarkRequestTest extends TestCase {

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new AddBookmarkRequest(new File("/")),
            new AddBookmarkRequest(new File("/")))
        .addEqualityGroup(
            new AddBookmarkRequest(new File("abc")),
            new AddBookmarkRequest(new File("abc")))
        .testEquals();
  }

}
