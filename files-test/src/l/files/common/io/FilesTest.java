package l.files.common.io;

import static java.util.Arrays.asList;
import static l.files.common.io.Files.getNonExistentDestinationFile;

import java.io.File;
import junit.framework.TestCase;
import l.files.test.TempDir;

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

  public void testGetNonExistentDestinationFile_renamesFileWithExtensionIfDestinationFileExists() {
    File source = dir.newFile("abc.txt");
    assertEquals(new File(dir.get(), "abc 2.txt"),
        getNonExistentDestinationFile(source, dir.get()));
  }

  public void testGetNonExistentDestinationFile_renamesFileWithoutExtensionIfDestinationFileExists() {
    File source = dir.newFile("abc");
    assertEquals(new File(dir.get(), "abc 2"),
        getNonExistentDestinationFile(source, dir.get()));
  }


  public void testGetNonExistentDestinationFile_renamesFileWithoutBaseNameIfDestinationFileExists() {
    File source = dir.newFile(".abc");
    assertEquals(new File(dir.get(), ".abc 2"),
        getNonExistentDestinationFile(source, dir.get()));
  }

  public void testGetNonExistentDestinationFile_renamesDirectoryIfDestinationFileExists() {
    File source = dir.newDir("abc.txt");
    assertEquals(new File(dir.get(), "abc.txt 2"),
        getNonExistentDestinationFile(source, dir.get()));
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
