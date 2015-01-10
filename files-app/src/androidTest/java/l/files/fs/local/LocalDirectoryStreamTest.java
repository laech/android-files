package l.files.fs.local;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import l.files.common.testing.FileBaseTest;
import l.files.fs.PathEntry;

import static com.google.common.collect.Lists.newArrayList;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class LocalDirectoryStreamTest extends FileBaseTest {

  public void testReturnCorrectEntries() throws Exception {
    File f1 = tmp().createFile("a");
    File f2 = tmp().createDir("b");
    File f3 = tmp().get("d");
    symlink(f1.getPath(), f3.getPath());

    try (LocalDirectoryStream stream = LocalDirectoryStream.open(tmp().get())) {
      List<PathEntry> expected = Arrays.<PathEntry>asList(
          LocalPathEntry.create(tmp().get(), lstat(f1.getPath()).ino(), f1.getName(), false),
          LocalPathEntry.create(tmp().get(), lstat(f2.getPath()).ino(), f2.getName(), true),
          LocalPathEntry.create(tmp().get(), lstat(f3.getPath()).ino(), f3.getName(), false)
      );
      List<PathEntry> actual = newArrayList(stream);
      assertEquals(expected, actual);
    }
  }

  public void testIteratorReturnsFalseIfNoNextElement() throws Exception {
    try (LocalDirectoryStream stream = LocalDirectoryStream.open(tmp().get())) {
      Iterator<PathEntry> iterator = stream.iterator();
      assertFalse(iterator.hasNext());
      assertFalse(iterator.hasNext());
    }
  }

  public void testIteratorThrowsNoSuchElementExceptionOnEmpty() throws Exception {
    try (LocalDirectoryStream stream = LocalDirectoryStream.open(tmp().get())) {
      stream.iterator().next();
      fail();
    } catch (NoSuchElementException e) {
      // Pass
    }
  }

  public void testIteratorMethodCannotBeReused() throws Exception {
    try (LocalDirectoryStream stream = LocalDirectoryStream.open(tmp().get())) {
      stream.iterator();
      try {
        stream.iterator();
        fail();
      } catch (IllegalStateException e) {
        // Pass
      }
    }
  }
}
