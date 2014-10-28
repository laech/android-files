package l.files.fs.local;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static l.files.fs.local.LocalDirectoryStream.Entry;
import static l.files.fs.local.LocalDirectoryStream.Entry.TYPE_DIR;
import static l.files.fs.local.LocalDirectoryStream.Entry.TYPE_LNK;
import static l.files.fs.local.LocalDirectoryStream.Entry.TYPE_REG;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class LocalDirectoryStreamTest extends FileBaseTest {

  public void testReturnCorrectEntries() throws Exception {
    File f1 = tmp().createFile("a");
    File f2 = tmp().createDir("b");
    File f3 = tmp().get("d");
    symlink(f1.getPath(), f3.getPath());

    try (LocalDirectoryStream stream = LocalDirectoryStream.open(tmp().get())) {
      List<Entry> expected = asList(
          Entry.create(tmp().get(), lstat(f1.getPath()).ino(), f1.getName(), TYPE_REG),
          Entry.create(tmp().get(), lstat(f2.getPath()).ino(), f2.getName(), TYPE_DIR),
          Entry.create(tmp().get(), lstat(f3.getPath()).ino(), f3.getName(), TYPE_LNK)
      );
      List<Entry> actual = newArrayList(stream);
      assertEquals(expected, actual);
    }
  }

  public void testIteratorReturnsFalseIfNoNextElement() throws Exception {
    try (LocalDirectoryStream stream = LocalDirectoryStream.open(tmp().get())) {
      Iterator<Entry> iterator = stream.iterator();
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
