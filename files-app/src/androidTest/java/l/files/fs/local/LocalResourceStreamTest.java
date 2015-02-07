package l.files.fs.local;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import l.files.common.testing.FileBaseTest;

import static java.util.Arrays.asList;
import static kotlin.KotlinPackage.toArrayList;
import static l.files.fs.local.Stat.lstat;
import static l.files.fs.local.Unistd.symlink;

public final class LocalResourceStreamTest extends FileBaseTest {

  public void testReturnCorrectEntries() throws Exception {
    File f1 = tmp().createFile("a");
    File f2 = tmp().createDir("b");
    File f3 = tmp().get("d");
    symlink(f1.getPath(), f3.getPath());

    try (LocalResourceStream stream = LocalResourceStream.open(tmpPath())) {
      List<LocalPathEntry> expected = asList(
          new LocalPathEntry(tmpPath().resolve("a"), lstat(f1.getPath()).getIno(), false),
          new LocalPathEntry(tmpPath().resolve("b"), lstat(f2.getPath()).getIno(), true),
          new LocalPathEntry(tmpPath().resolve("d"), lstat(f3.getPath()).getIno(), false)
      );
      List<LocalPathEntry> actual = toArrayList(stream);
      assertEquals(expected, actual);
    }
  }

  public void testIteratorReturnsFalseIfNoNextElement() throws Exception {
    try (LocalResourceStream stream = LocalResourceStream.open(tmpPath())) {
      Iterator<?> iterator = stream.iterator();
      assertFalse(iterator.hasNext());
      assertFalse(iterator.hasNext());
    }
  }

  public void testIteratorThrowsNoSuchElementExceptionOnEmpty() throws Exception {
    try (LocalResourceStream stream = LocalResourceStream.open(tmpPath())) {
      stream.iterator().next();
      fail();
    } catch (NoSuchElementException e) {
      // Pass
    }
  }

  public void testIteratorMethodCannotBeReused() throws Exception {
    try (LocalResourceStream stream = LocalResourceStream.open(tmpPath())) {
      stream.iterator();
      try {
        stream.iterator();
        fail();
      } catch (IllegalStateException e) {
        // Pass
      }
    }
  }

  private LocalPath tmpPath() {return LocalPath.of(tmp().get());}
}
