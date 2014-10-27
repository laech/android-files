package l.files.fs;

import java.io.File;
import java.net.URI;

import l.files.common.testing.FileBaseTest;

import static org.apache.commons.io.FileUtils.forceDelete;

public final class FileIdTest extends FileBaseTest {

  public void testCreateIdFromDirectoryReturnsSameValueBeforeAfterDeletion() throws Exception {
    File dir = tmp().createDir("dir");
    FileId before = FileId.of(dir);
    forceDelete(dir);
    FileId after = FileId.of(dir);
    assertEquals(before, after);
    assertEquals(before.toString(), after.toString());
  }

  public void testCreate() throws Exception {
    File a = tmp().createDir("a");
    File b = tmp().createFile("b");
    assertEquals("file:" + tmp().get("a").getPath(), FileId.of(a).toString());
    assertEquals("file:" + tmp().get("b").getPath(), FileId.of(b).toString());
    assertEquals("file:/", FileId.of(new File("/")).toString());
    assertEquals("file:/c/hello", FileId.of(new File("/c/b/../hello")).toString());
    assertEquals("file:/c/hello", FileId.of(new File("/c/./hello")).toString());
  }

  public void testToString_isValidUri() throws Exception {
    // No exception
    new URI(FileId.of(tmp().createFile("Hello World")).toString());
  }

  public void testScheme() throws Exception {
    assertEquals(Scheme.parse("file"), FileId.of(tmp().get()).scheme());
  }
}
