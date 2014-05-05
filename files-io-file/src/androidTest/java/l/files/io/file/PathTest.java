package l.files.io.file;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static org.apache.commons.io.FileUtils.getTempDirectory;

public final class PathTest extends FileBaseTest {

  public void testChild() {
    assertEquals("/a/b", Path.from("/a").child("b").toString());
    assertEquals("/a/b", Path.from("/a").child("/b").toString());
    assertEquals("/a", Path.from("/").child("a").toString());
    assertEquals("/a/b/c", Path.from("/a").child("b/c").toString());
  }

  public void testParent() {
    assertEquals(null, Path.ROOT.parent());
    assertEquals("/", Path.from("/a").parent().toString());
    assertEquals("/a/b", Path.from("/a/b/c").parent().toString());
  }

  public void testName() {
    assertEquals("", Path.ROOT.name());
    assertEquals("a", Path.from("/a").name());
    assertEquals("a.txt", Path.from("/b/c/a.txt").name());
  }

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

  public void testStartsWith() {
    testStartsWith(true, "/a/b", "/a/b");
    testStartsWith(true, "/parent", "/parent/child/x");
    testStartsWith(false, "/a", "/ab");
    testStartsWith(false, "/abc", "/xyz0");
  }

  private void testStartsWith(boolean expected, String parent, String child) {
    assertEquals(expected, Path.from(child).startsWith(Path.from(parent)));
  }
}
