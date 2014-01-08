package l.files.common.io;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Set;

import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

import static l.files.common.io.Files.getNonExistentDestinationFile;
import static l.files.common.io.Files.hierarchy;
import static l.files.common.io.Files.isAncestorOrSelf;
import static l.files.common.io.Files.replace;

public final class FilesTest extends BaseTest {

  private TempDir temp;

  @Override protected void setUp() throws Exception {
    super.setUp();
    temp = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    temp.delete();
    super.tearDown();
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
    testExistent(temp.newFile("a"), "a 2");
    testExistent(temp.newFile("b.txt"), "b 2.txt");
    testExistent(temp.newFile("c 2.mp4"), "c 3.mp4");
    testExistent(temp.newFile(".mp4"), "2.mp4");
    testExistent(temp.newFile("d 2"), "d 3");
    testExistent(temp.newFile("dir/x"), "x");
  }

  public void testGetNonExistentDestinationFile_directory() {
    testExistent(temp.newDirectory("a"), "a 2");
    testExistent(temp.newDirectory("b.txt"), "b.txt 2");
    testExistent(temp.newDirectory("c 2.mp4"), "c 2.mp4 2");
    testExistent(temp.newDirectory(".mp4"), ".mp4 2");
    testExistent(temp.newDirectory("a2"), "a2 2");
    testExistent(temp.newDirectory("a 3"), "a 4");
    testExistent(temp.newDirectory("d 2"), "d 3");
    testExistent(temp.newDirectory("dir/x"), "x");
  }

  private void testExistent(File file, String expectedName) {
    File expected = new File(temp.get(), expectedName);
    File actual = getNonExistentDestinationFile(file, temp.get());
    assertEquals(expected, actual);
  }
}
