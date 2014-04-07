package l.files.io;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static org.apache.commons.io.FileUtils.getTempDirectory;

public final class PathTest extends FileBaseTest {

  public void testFrom_file_noEndSeparatorRegardlessOfExistence() {
    File dir = new File(getTempDirectory(), "tmp");

    assertTrue(dir.mkdir());
    Path p1 = Path.from(dir);

    assertTrue(dir.delete());
    Path p2 = Path.from(dir);

    assertFalse(p1.toString().endsWith("/"));
    assertFalse(p2.toString().endsWith("/"));
    assertEquals(p1, p2);
  }

  public void testFrom_file_normalized() {
    Path expected = Path.from("/a/b");
    Path actual = Path.from(new File("/a/../a/./b"));
    assertEquals(expected, actual);
  }

  public void testFrom_string_normalized() {
    Path expected = Path.from(new File("a/..").getAbsolutePath());
    Path actual = Path.from("a/..");
    assertEquals(expected, actual);
  }

  public void testStartsWith_true_equalPaths() {
    testStartsWith(true, "/a/b", "/a/b");
  }

  public void testStartsWith_true_parentPathMatches() {
    testStartsWith(true, "/parent", "/parent/child/x");
  }

  public void testStartsWith_false_checkParentNotString() {
    testStartsWith(false, "/a", "/ab");
  }

  public void testStartsWith_false_unrelatedPaths() {
    testStartsWith(false, "/abc", "/xyz0");
  }

  private void testStartsWith(boolean expected, String parent, String child) {
    assertEquals(expected, Path.from(child).startsWith(Path.from(parent)));
  }
}
