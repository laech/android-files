package l.files.operations;

import java.io.File;
import java.util.List;
import java.util.Set;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static l.files.fs.local.Files.symlink;

public final class DeleteTest extends FileBaseTest {

  public void testNotifiesListener() throws Exception {
    File src = tmp().createDir("a");
    tmp().createFile("a/b");

    Set<File> expected = newHashSet(
        tmp().get("a"),
        tmp().get("a/b")
    );

    Delete delete = create(asList(src.getPath()));
    delete.execute();

    assertEquals(delete.getDeletedItemCount(), expected.size());
  }

  public void testDeletesFile() throws Exception {
    File file = tmp().createFile("a");
    delete(file);
    assertFalse(file.exists());
  }

  public void testDeletesNonEmptyDirectory() throws Exception {
    File dir = tmp().createDir("a");
    File file = tmp().createFile("a/child.txt");
    delete(dir);
    assertFalse(file.exists());
    assertFalse(dir.exists());
  }

  public void testDeletesEmptyDirectory() throws Exception {
    File dir = tmp().createDir("a");
    delete(dir);
    assertFalse(dir.exists());
  }

  public void testDeletesSymbolicLinkButNotLinkedFile() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    symlink(a.getPath(), b.getPath());
    delete(b);
    assertFalse(b.exists());
    assertTrue(a.exists());
  }

  public void testReturnsFailures() throws Exception {
    File a = tmp().createFile("a");
    assertTrue(tmp().get().setWritable(false));

    List<Failure> failures = null;
    try {
      delete(a);
      fail();
    } catch (FileException e) {
      failures = e.failures();
    }
    assertEquals(a.getPath(), failures.get(0).path());
    assertEquals(1, failures.size());
  }

  private void delete(File file) throws Exception {
    create(asList(file.getPath())).execute();
  }

  private Delete create(Iterable<String> paths) {
    return new Delete(paths);
  }
}
