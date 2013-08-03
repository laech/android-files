package l.files.app;

import junit.framework.TestCase;

import java.io.File;

public final class OpenFileRequestTest extends TestCase {

  public void testEquals() {
    OpenFileRequest a1 = new OpenFileRequest(new File("/"));
    OpenFileRequest a2 = new OpenFileRequest(new File("/"));
    OpenFileRequest b = new OpenFileRequest(new File("/abc"));
    assertTrue(a1.equals(a1));
    assertTrue(a1.equals(a2));
    assertFalse(a1.equals(b));
    assertFalse(a1.equals(null));
  }
}
