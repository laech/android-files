package l.files.util;

import android.os.Build;
import android.test.AndroidTestCase;
import l.files.R;

import java.io.File;

import static java.io.File.createTempFile;
import static l.files.util.FileSystem.DIRECTORY_HOME;
import static l.files.util.FileSystem.DIRECTORY_ROOT;

public final class FileSystemTest extends AndroidTestCase {

  private File file;
  private FileSystem fileSystem;

  @Override protected void setUp() throws Exception {
    super.setUp();
    fileSystem = new FileSystem();
    file = createTempFile("abc", "def");
    assertTrue(file.delete());
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    assertTrue(file.delete() || !file.exists());
  }

  public void testGetsNameForHomeDirectory() {
    assertEquals(
        getContext().getString(R.string.home),
        fileSystem.getDisplayName(DIRECTORY_HOME, getContext().getResources()));
  }

  public void testGetsNameForRootDirectory() {
    assertEquals(
        Build.MODEL,
        fileSystem.getDisplayName(DIRECTORY_ROOT, getContext().getResources()));
  }
}
