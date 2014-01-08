package l.files.common;

import java.io.File;

import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

import static l.files.common.io.Files.getNonExistentDestinationFile;

public final class FilesTest extends BaseTest {

  private TempDir tempDir;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tempDir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    tempDir.delete();
    super.tearDown();
  }

  public void testGetNonExistentDestinationFile_file() {
    testGetNonExistentDestinationFile(tempDir.newFile("a"), "a 2");
    testGetNonExistentDestinationFile(tempDir.newFile("b.txt"), "b 2.txt");
    testGetNonExistentDestinationFile(tempDir.newFile("c 2.mp4"), "c 3.mp4");
    testGetNonExistentDestinationFile(tempDir.newFile(".mp4"), "2.mp4");
    testGetNonExistentDestinationFile(tempDir.newFile("d 2"), "d 3");
    testGetNonExistentDestinationFile(tempDir.newFile("dir/x"), "x");
  }

  public void testGetNonExistentDestinationFile_directory() {
    testGetNonExistentDestinationFile(tempDir.newDirectory("a"), "a 2");
    testGetNonExistentDestinationFile(tempDir.newDirectory("b.txt"), "b.txt 2");
    testGetNonExistentDestinationFile(tempDir.newDirectory("c 2.mp4"), "c 2.mp4 2");
    testGetNonExistentDestinationFile(tempDir.newDirectory(".mp4"), ".mp4 2");
    testGetNonExistentDestinationFile(tempDir.newDirectory("a2"), "a2 2");
    testGetNonExistentDestinationFile(tempDir.newDirectory("a 3"), "a 4");
    testGetNonExistentDestinationFile(tempDir.newDirectory("d 2"), "d 3");
    testGetNonExistentDestinationFile(tempDir.newDirectory("dir/x"), "x");
  }

  private void testGetNonExistentDestinationFile(File file, String expectedName) {
    File expected = new File(tempDir.get(), expectedName);
    File actual = getNonExistentDestinationFile(file, tempDir.get());
    assertEquals(expected, actual);
  }
}
