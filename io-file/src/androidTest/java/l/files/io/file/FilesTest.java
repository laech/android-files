package l.files.io.file;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import l.files.common.testing.FileBaseTest;

import static l.files.io.file.Files.checkExist;
import static l.files.io.file.Files.checkReadable;
import static l.files.io.file.Files.checkWritable;
import static l.files.io.file.Files.getNonExistentDestinationFile;
import static l.files.io.file.Files.hierarchy;
import static l.files.io.file.Files.isAncestorOrSelf;
import static l.files.io.file.Files.replace;
import static melody.Assertions.test;
import static melody.assertions.java.lang.Objects.equalTo;

public final class FilesTest extends FileBaseTest {

  public void testCheckReadableForReadablePath() throws Exception {
    checkReadable(tmp().get().getPath());
  }

  public void testCheckReadableForNonReadablePath() throws Exception {
    File file = tmp().createFile("abc");
    test(file.setReadable(false), equalTo(true));
    try {
      checkReadable(file.getPath());
      fail();
    } catch (IOException e) {
      // Pass
    }

  }

  public void testCheckReadableForNonExistentPath() throws Exception {
    try {
      checkReadable("abc");
      fail();
    } catch (IOException e) {
      // Pass
    }
  }

  public void testCheckWritableForWritablePath() throws Exception {
    checkWritable(tmp().get().getPath());
  }

  public void testCheckWritableForNonExistentPath() throws Exception {
    try {
      checkWritable("abc");
      fail();
    } catch (IOException e) {
      // Pass
    }
  }

  public void testCheckWritableForNonWritablePath() throws Exception {
    File file = tmp().createFile("a");
    test(file.setWritable(false), equalTo(true));
    try {
      checkWritable(file.getPath());
      fail();
    } catch (IOException e) {
      // Pass
    }
  }

  public void testCheckExistForExistingPath() throws Exception {
    checkExist(tmp().createDir("a").getPath());
  }

  public void testCheckExistForNonExistingPath() throws Exception {
    try {
      checkExist("abc");
      fail();
    } catch (IOException e) {
      // Pass
    }
  }

  public void testHierarchy() throws Exception {
    Set<File> expected = ImmutableSet.of(
        new File("/"),
        new File("/a"),
        new File("/a/b"),
        new File("/a/b/c.txt"));
    Set<File> actual = hierarchy(new File("a/b/c.txt"));
    assertEquals(expected, actual);
  }

  public void testIsAncestorOrSelf_true_self() throws Exception {
    File file = new File("/a/b");
    assertTrue(isAncestorOrSelf(file, file));
  }

  public void testIsAncestorOrSelf_true_ancestor() throws Exception {
    File file = new File("/a/b");
    File ancestor = new File("/a");
    assertTrue(isAncestorOrSelf(file, ancestor));
  }

  public void testIsAncestorOrSelf_false() throws Exception {
    File file = new File("/a/b");
    File ancestor = new File("/b");
    assertFalse(isAncestorOrSelf(file, ancestor));
  }

  public void testReplace() {
    File file = new File("/a/b/c.txt");
    File match = new File("/a");
    File replacement = new File("/1/2/3/4");
    File expected = new File("/1/2/3/4/b/c.txt");
    File actual = replace(file, match, replacement);
    assertEquals(expected, actual);
  }

  public void testGetNonExistentDestinationFile_file() {
    testExistent(tmp().createFile("a"), "a 2");
    testExistent(tmp().createFile("b.txt"), "b 2.txt");
    testExistent(tmp().createFile("c 2.mp4"), "c 3.mp4");
    testExistent(tmp().createFile(".mp4"), "2.mp4");
    testExistent(tmp().createFile("d 2"), "d 3");
    testExistent(tmp().createFile("dir/x"), "x");
  }

  public void testGetNonExistentDestinationFile_directory() {
    testExistent(tmp().createDir("a"), "a 2");
    testExistent(tmp().createDir("b.txt"), "b.txt 2");
    testExistent(tmp().createDir("c 2.mp4"), "c 2.mp4 2");
    testExistent(tmp().createDir(".mp4"), ".mp4 2");
    testExistent(tmp().createDir("a2"), "a2 2");
    testExistent(tmp().createDir("a 3"), "a 4");
    testExistent(tmp().createDir("d 2"), "d 3");
    testExistent(tmp().createDir("dir/x"), "x");
  }

  private void testExistent(File file, String expectedName) {
    File expected = new File(tmp().get(), expectedName);
    File actual = getNonExistentDestinationFile(file, tmp().get());
    assertEquals(expected, actual);
  }
}
