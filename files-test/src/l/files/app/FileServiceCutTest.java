package l.files.app;

import static l.files.app.FileService.Cut;
import static l.files.app.FileService.cut;

import java.io.File;

public final class FileServiceCutTest extends FileServiceTest<Cut> {

  public FileServiceCutTest() {
    super(Cut.class);
  }

  public void testCutsFile() {
    File src = dir.newFile();
    File dst = dir.newDir();

    startService(cut(getContext(), src, dst));

    waitForExistence(new File(dst, src.getName()));
    assertFalse(src.exists());
  }

  public void testCutsDir() throws Exception {
    File dst = dir.newDir();
    File src = dir.newDir();
    assertTrue(new File(src, "1").createNewFile());
    assertTrue(new File(src, "2").createNewFile());

    startService(cut(getContext(), src, dst));

    File expected = new File(dst, src.getName());
    waitForExistence(new File(expected, "1"));
    waitForExistence(new File(expected, "2"));
    assertFalse(src.exists());
  }

  public void testRenamesFileIfDestinationFileExists() throws Exception {
    File src = dir.newFile("a");
    File dst = dir.newDir();
    assertTrue(new File(dst, "a").createNewFile());

    startService(cut(getContext(), src, dst));

    File expected = new File(dst, "a 2");
    waitForExistence(expected);
    assertTrue(expected.isFile());
  }

  public void testRenamesDirIfDestinationDirExists() throws Exception {
    File src = dir.newDir("a");
    File dst = dir.newDir();
    assertTrue(new File(dst, "a").mkdir());

    startService(cut(getContext(), src, dst));

    File expected = new File(dst, "a 2");
    waitForExistence(expected);
    assertTrue(expected.isDirectory());
  }
}
