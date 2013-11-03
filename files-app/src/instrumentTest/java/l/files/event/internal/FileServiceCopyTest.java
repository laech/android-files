package l.files.event.internal;

import static l.files.event.internal.FileService.Copy;
import static l.files.event.internal.FileService.copy;

import java.io.File;
import java.io.IOException;

public final class FileServiceCopyTest extends FileServiceTest<Copy> {

  public FileServiceCopyTest() {
    super(Copy.class);
  }

  public void testCopiesFile() {
    File src = dir.newFile();
    File dst = dir.newDir();

    startService(copy(getContext(), src, dst));

    waitForExistence(new File(dst, src.getName()));
    assertTrue(src.exists());
  }

  public void testCopiesDir() throws IOException {
    File dst = dir.newDir();
    File src = dir.newDir();
    assertTrue(new File(src, "1").createNewFile());
    assertTrue(new File(src, "2").createNewFile());

    startService(copy(getContext(), src, dst));

    File expected = new File(dst, src.getName());
    waitForExistence(new File(expected, "1"));
    waitForExistence(new File(expected, "2"));
    assertTrue(src.exists());
    assertTrue(new File(src, "1").exists());
    assertTrue(new File(src, "2").exists());
  }

  public void testRenamesExtensionLessFileIfDestinationFileExists() throws Exception {
    File src = dir.newFile("a");
    File dst = dir.newDir();
    assertTrue(new File(dst, "a").createNewFile());

    startService(copy(getContext(), src, dst));

    File expected = new File(dst, "a 2");
    waitForExistence(expected);
    assertTrue(expected.isFile());
    testRenamesFile("a", "a 2");
  }

  public void testRenamesFileWithExtensionIfDestinationFileExists() throws Exception {
    testRenamesFile("a.txt", "a 2.txt");
  }

  private void testRenamesFile(String original, String expected) throws IOException {
    File src = dir.newFile(original);
    File dst = dir.newDir();
    assertTrue(new File(dst, original).createNewFile());

    startService(copy(getContext(), src, dst));

    File file = new File(dst, expected);
    waitForExistence(file);
    assertTrue(file.isFile());
  }

  public void testRenamesDirIfDestinationDirExists() throws Exception {
    File src = dir.newDir("a");
    File dst = dir.newDir();
    assertTrue(new File(dst, "a").mkdir());

    startService(copy(getContext(), src, dst));

    File expected = new File(dst, "a 2");
    waitForExistence(expected);
    assertTrue(expected.isDirectory());
  }
}
