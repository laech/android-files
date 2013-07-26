package l.files;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import l.files.event.BookmarksEvent;

import java.io.File;

public final class BookmarksEventTest extends TestCase {

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BookmarksEvent(),
            new BookmarksEvent())
        .addEqualityGroup(
            new BookmarksEvent(new File("/")),
            new BookmarksEvent(new File("/")))
        .addEqualityGroup(
            new BookmarksEvent(new File("abc")),
            new BookmarksEvent(new File("abc")))
        .addEqualityGroup(
            new BookmarksEvent(new File("/"), new File("abc")),
            new BookmarksEvent(new File("/"), new File("abc")))
        .testEquals();
  }

}
