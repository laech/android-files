package l.files;

import com.google.common.testing.EqualsTester;
import junit.framework.TestCase;
import l.files.event.OpenFileRequest;

import java.io.File;

public final class OpenFileRequestTest extends TestCase {

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new OpenFileRequest(new File("/")),
            new OpenFileRequest(new File("/")))
        .addEqualityGroup(
            new OpenFileRequest(new File("abc")),
            new OpenFileRequest(new File("abc")))
        .testEquals();
  }

}
