package l.files.io.file;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static l.files.io.file.DirectoryStream.Entry;
import static l.files.io.file.DirectoryStream.Entry.TYPE_DIR;
import static l.files.io.file.DirectoryStream.Entry.TYPE_LNK;
import static l.files.io.file.DirectoryStream.Entry.TYPE_REG;
import static l.files.io.os.Stat.lstat;
import static l.files.io.os.Unistd.symlink;

public final class DirectoryStreamTest extends FileBaseTest {

  public void testReturnCorrectEntries() throws Exception {
    File f1 = tmp().createFile("a");
    File f2 = tmp().createDir("b");
    File f3 = tmp().get("d");
    symlink(f1.getPath(), f3.getPath());

    DirectoryStream stream = DirectoryStream.open(tmp().get().getPath());
    try {

      List<Entry> expected = asList(
          Entry.create(lstat(f1.getPath()).ino, f1.getName(), TYPE_REG),
          Entry.create(lstat(f2.getPath()).ino, f2.getName(), TYPE_DIR),
          Entry.create(lstat(f3.getPath()).ino, f3.getName(), TYPE_LNK)
      );
      List<Entry> actual = newArrayList(stream);
      assertEquals(expected, actual);

    } finally {
      stream.close();
    }
  }

  public void testIteratorReturnsFalseIfNoNextElement() throws Exception {
    DirectoryStream stream = DirectoryStream.open(tmp().get().getPath());
    try {
      Iterator<Entry> iterator = stream.iterator();
      assertFalse(iterator.hasNext());
      assertFalse(iterator.hasNext());
    } finally {
      stream.close();
    }
  }

  public void testIteratorThrowsNoSuchElementExceptionOnEmpty() throws Exception {
    DirectoryStream stream = DirectoryStream.open(tmp().get().getPath());
    try {
      stream.iterator().next();
      fail();
    } catch (NoSuchElementException e) {
      // Pass
    } finally {
      stream.close();
    }
  }

  public void testIteratorMethodCannotBeReused() throws Exception {
    DirectoryStream stream = DirectoryStream.open(tmp().get().getPath());
    try {
      stream.iterator();
      try {
        stream.iterator();
        fail();
      } catch (IllegalStateException e) {
        // Pass
      }
    } finally {
      stream.close();
    }
  }
}
