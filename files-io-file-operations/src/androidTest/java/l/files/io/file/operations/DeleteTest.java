package l.files.io.file.operations;

import java.io.File;
import java.util.List;

import l.files.common.testing.FileBaseTest;
import l.files.io.file.FileInfo;

import static java.util.Arrays.asList;
import static l.files.io.file.FileInfo.symlink;
import static l.files.io.file.operations.Delete.Listener;
import static l.files.io.file.operations.FileOperation.Failure;

public final class DeleteTest extends FileBaseTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override public void onDelete(FileInfo file) {}
  };

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

    List<Failure> failures = delete(a);
    assertEquals(a.getPath(), failures.get(0).path());
    assertEquals(1, failures.size());
  }

  private List<Failure> delete(File file) throws Exception {
    return create(NULL_LISTENER, asList(file.getPath())).call();
  }

  private Delete create(Listener listener, Iterable<String> paths) {
    return new Delete(listener, paths);
  }
}
