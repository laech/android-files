package l.files.util;

import static java.io.File.createTempFile;
import static l.files.util.FileSystem.DIRECTORY_HOME;
import static l.files.util.FileSystem.DIRECTORY_ROOT;

import java.io.File;

import l.files.R;
import android.os.Build;
import android.test.AndroidTestCase;

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

  public void testHasNoPermissionToReadUnexecutableDirectory() throws Exception {
    assertTrue(file.mkdir());
    assertTrue(file.setExecutable(false, false));
    assertFalse(fileSystem.hasPermissionToRead(file));
  }

  public void testHasNoPermissionToReadUnreadableFile() throws Exception {
    assertTrue(file.createNewFile());
    assertTrue(file.setReadable(false, false));
    assertFalse(fileSystem.hasPermissionToRead(file));
  }

  public void testHasNoPermissionToReadUnreadableDirectory() throws Exception {
    assertTrue(file.mkdir());
    assertTrue(file.setReadable(false, false));
    assertFalse(fileSystem.hasPermissionToRead(file));

  }

  public void testHasPermissionToReadReadableFile() throws Exception {
    assertTrue(file.createNewFile());
    assertTrue(file.setReadable(true, true));
    assertTrue(fileSystem.hasPermissionToRead(file));
  }

  public void testHasPermissionToReadReadableDirectory() {
    assertTrue(file.mkdir());
    assertTrue(file.setReadable(true, true));
    assertTrue(fileSystem.hasPermissionToRead(file));
  }
}
