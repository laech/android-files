package l.files.fs.local;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.getTempDirectory;

public final class LocalPathTest extends FileBaseTest {

  public void testGetName() throws Exception {
    assertEquals("a", LocalPath.of("/a/").getName());
    assertEquals("a", LocalPath.of("/a").getName());
    assertEquals("a", LocalPath.of("a").getName());
    assertEquals("", LocalPath.of("/").getName());
  }

  public void testCreateFromDirectoryReturnsSameValueBeforeAfterDeletion() throws Exception {
    File dir = tmp().createDir("dir");
    LocalPath before = LocalPath.of(dir);
    forceDelete(dir);
    LocalPath after = LocalPath.of(dir);
    assertEquals(before, after);
    assertEquals(before.getUri(), after.getUri());
    assertEquals(before.getFile(), after.getFile());
    assertEquals(before.toString(), after.toString());
  }

  public void testCreateToUri() throws Exception {
    File a = tmp().createDir("a");
    File b = tmp().createFile("b");
    assertEquals("file:" + tmp().get("a").getPath(), LocalPath.of(a).getUri().toString());
    assertEquals("file:" + tmp().get("b").getPath(), LocalPath.of(b).getUri().toString());
    assertEquals("file:/", LocalPath.of(new File("/")).getUri().toString());
    assertEquals("file:/c/hello", LocalPath.of(new File("/c/b/../hello")).getUri().toString());
    assertEquals("file:/c/hello", LocalPath.of(new File("/c/./hello")).getUri().toString());
  }

  public void testResolve() {
    assertEquals("/a/b", LocalPath.of("/a").resolve("b").toString());
    assertEquals("/a/b", LocalPath.of("/a").resolve("/b").toString());
    assertEquals("/a", LocalPath.of("/").resolve("a").toString());
    assertEquals("/a/b/c", LocalPath.of("/a").resolve("b/c").toString());
    assertEquals("/a", LocalPath.of("/a").resolve(".").toString());
    assertEquals("/b", LocalPath.of("/a").resolve("../b").toString());
  }

  public void testParent() {
    assertEquals(null, LocalPath.of(new File("/")).getParent());
    assertEquals("/", LocalPath.of("/a").getParent().toString());
    assertEquals("/a/b", LocalPath.of("/a/b/c").getParent().toString());
  }

  public void testFrom_file_noEndSeparatorRegardlessOfExistence() {
    File dir = new File(getTempDirectory(), "tmp");

    assertTrue(dir.mkdir());
    LocalPath p1 = LocalPath.of(dir);

    assertTrue(dir.delete());
    LocalPath p2 = LocalPath.of(dir);

    assertFalse(p1.getUri().toString().endsWith("/"));
    assertFalse(p2.getUri().toString().endsWith("/"));
    assertEquals(p1, p2);
  }

  public void testFrom_file_normalized() {
    LocalPath expected = LocalPath.of("/a/b");
    LocalPath actual = LocalPath.of(new File("/a/../a/./b"));
    assertEquals(expected, actual);
  }

  public void testFrom_string_normalized() {
    LocalPath expected = LocalPath.of(new File("a/..").getAbsolutePath());
    LocalPath actual = LocalPath.of("a/..");
    assertEquals(expected, actual);
  }

  public void testStartsWith() {
    testStartsWith(true, "/a/b", "/a/b");
    testStartsWith(true, "/parent", "/parent/child/x");
    testStartsWith(false, "/a", "/ab");
    testStartsWith(false, "/abc", "/xyz0");
    testStartsWith(true, "/", "/z");
  }

  private void testStartsWith(boolean expected, String parent, String child) {
    assertEquals(expected, LocalPath.of(child).startsWith(LocalPath.of(parent)));
  }
}
