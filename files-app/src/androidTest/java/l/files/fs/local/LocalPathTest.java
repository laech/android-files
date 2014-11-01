package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static org.apache.commons.io.FileUtils.getTempDirectory;

public final class LocalPathTest extends FileBaseTest {

  public void testChild() {
    assertEquals("/a/b", LocalPath.from("/a").child("b").toString());
    assertEquals("/a/b", LocalPath.from("/a").child("/b").toString());
    assertEquals("/a", LocalPath.from("/").child("a").toString());
    assertEquals("/a/b/c", LocalPath.from("/a").child("b/c").toString());
  }

  public void testParent() {
    assertEquals(null, LocalPath.ROOT.parent());
    assertEquals("/", LocalPath.from("/a").parent().toString());
    assertEquals("/a/b", LocalPath.from("/a/b/c").parent().toString());
  }

  public void testName() {
    assertEquals("", LocalPath.ROOT.name());
    assertEquals("a", LocalPath.from("/a").name());
    assertEquals("a.txt", LocalPath.from("/b/c/a.txt").name());
  }

  public void testFrom_file_noEndSeparatorRegardlessOfExistence() {
    File dir = new File(getTempDirectory(), "tmp");

    assertTrue(dir.mkdir());
    LocalPath p1 = LocalPath.from(dir);

    assertTrue(dir.delete());
    LocalPath p2 = LocalPath.from(dir);

    assertFalse(p1.toString().endsWith("/"));
    assertFalse(p2.toString().endsWith("/"));
    assertEquals(p1, p2);
  }

  public void testFrom_file_normalized() {
    LocalPath expected = LocalPath.from("/a/b");
    LocalPath actual = LocalPath.from(new File("/a/../a/./b"));
    assertEquals(expected, actual);
  }

  public void testFrom_string_normalized() {
    LocalPath expected = LocalPath.from(new File("a/..").getAbsolutePath());
    LocalPath actual = LocalPath.from("a/..");
    assertEquals(expected, actual);
  }

  public void testStartsWith() {
    testStartsWith(true, "/a/b", "/a/b");
    testStartsWith(true, "/parent", "/parent/child/x");
    testStartsWith(false, "/a", "/ab");
    testStartsWith(false, "/abc", "/xyz0");
  }

  private void testStartsWith(boolean expected, String parent, String child) {
    assertEquals(expected, LocalPath.from(child).startsWith(LocalPath.from(parent)));
  }
}
