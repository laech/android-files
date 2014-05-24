package l.files.io.file.operations;

import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.List;
import java.util.Set;

import l.files.common.testing.FileBaseTest;
import l.files.io.file.FileInfo;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static l.files.io.file.Files.symlink;
import static l.files.io.file.operations.Delete.Listener;
import static l.files.io.file.operations.FileOperation.Failure;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class DeleteTest extends FileBaseTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override public void onDelete(FileInfo file) {}
  };

  public void testNotifiesListener() throws Exception {
    File src = tmp().createDir("a");
    tmp().createFile("a/b");

    Listener listener = mock(Listener.class);
    ArgumentCaptor<FileInfo> captor = ArgumentCaptor.forClass(FileInfo.class);
    Set<FileInfo> expected = newHashSet(
        FileInfo.get(tmp().get("a").getPath()),
        FileInfo.get(tmp().get("a/b").getPath())
    );

    create(listener, asList(src.getPath())).run();
    verify(listener, atLeastOnce()).onDelete(captor.capture());

    Set<FileInfo> actual = newHashSet(captor.getAllValues());
    assertEquals(expected, actual);
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
    create(NULL_LISTENER, asList(file.getPath())).run();
  }

  private Delete create(Listener listener, Iterable<String> paths) {
    return new Delete(listener, paths);
  }
}
