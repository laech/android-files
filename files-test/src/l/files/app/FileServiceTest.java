package l.files.app;

import static android.os.SystemClock.sleep;
import static l.files.app.FileService.copy;
import static l.files.app.FileService.cut;

import android.test.ServiceTestCase;
import java.io.File;
import java.io.IOException;
import l.files.test.TempDir;

public final class FileServiceTest extends ServiceTestCase<FileService> {

  private TempDir dir;

  public FileServiceTest() {
    super(FileService.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  public void testCutsFile() {
    File src = dir.newFile();
    File dst = dir.newDir();

    startService(cut(src, dst, getContext()));

    waitForExistence(new File(dst, src.getName()));
    assertFalse(src.exists());
  }

  public void testCutsDir() throws Exception {
    File dst = dir.newDir();
    File src = dir.newDir();
    assertTrue(new File(src, "1").createNewFile());
    assertTrue(new File(src, "2").createNewFile());

    startService(cut(src, dst, getContext()));

    File expected = new File(dst, src.getName());
    waitForExistence(new File(expected, "1"));
    waitForExistence(new File(expected, "2"));
    assertFalse(src.exists());
  }

  public void testCopiesFile() {
    File src = dir.newFile();
    File dst = dir.newDir();

    startService(copy(src, dst, getContext()));

    waitForExistence(new File(dst, src.getName()));
    assertTrue(src.exists());
  }

  public void testCopiesDir() throws IOException {
    File dst = dir.newDir();
    File src = dir.newDir();
    assertTrue(new File(src, "1").createNewFile());
    assertTrue(new File(src, "2").createNewFile());

    startService(copy(src, dst, getContext()));

    File expected = new File(dst, src.getName());
    waitForExistence(new File(expected, "1"));
    waitForExistence(new File(expected, "2"));
    assertTrue(src.exists());
    assertTrue(new File(src, "1").exists());
    assertTrue(new File(src, "2").exists());
  }

  public void testRenamesFileIfDestinationFileExists() throws Exception {
    File src = dir.newFile("a");
    File dst = dir.newDir();
    assertTrue(new File(dst, "a").createNewFile());

    startService(cut(src, dst, getContext()));

    File expected = new File(dst, "a 2");
    waitForExistence(expected);
    assertTrue(expected.isFile());
  }

  public void testRenamesDirIfDestinationDirExists() throws Exception {
    File src = dir.newDir("a");
    File dst = dir.newDir();
    assertTrue(new File(dst, "a").mkdir());

    startService(copy(src, dst, getContext()));

    File expected = new File(dst, "a 2");
    waitForExistence(expected);
    assertTrue(expected.isDirectory());
  }

  private void waitForExistence(File file) {
    for (int count = 0; count < 10 && !file.exists(); ++count) {
      sleep(10);
    }
    assertTrue(file.exists());
  }
}
