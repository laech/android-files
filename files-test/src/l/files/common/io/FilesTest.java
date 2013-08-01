package l.files.common.io;

import junit.framework.TestCase;
import l.files.test.TempDir;

import java.io.File;

import static java.util.Arrays.asList;

public final class FilesTest extends TestCase {

  private TempDir dir;

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    dir.delete();
  }

  public void testListFiles_showHiddenFiles() {
    File f1 = dir.newFile(".a");
    File f2 = dir.newFile("b");
    assertEquals(asList(f1, f2), asList(Files.listFiles(dir.get(), true)));
  }

  public void testListFiles_hideHiddenFiles() {
    dir.newFile(".a");
    File f = dir.newFile("b");
    assertEquals(asList(f), asList(Files.listFiles(dir.get(), false)));
  }
}
