package l.files.fs.local;

import java.io.File;
import java.util.Set;

import l.files.common.testing.FileBaseTest;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.fs.local.Dirent.DT_DIR;
import static l.files.fs.local.Dirent.DT_REG;
import static l.files.fs.local.Dirent.closedir;
import static l.files.fs.local.Dirent.opendir;
import static l.files.fs.local.Dirent.readdir;
import static l.files.fs.local.Stat.stat;

public final class DirentTest extends FileBaseTest {

  public void testOpenCloseDir() throws Exception {
    String path = tmp().get().getPath();
    long dir = opendir(path);
    closedir(dir);
  }

  public void testOpendirThrowsExceptionOnError() {
    try {
      opendir("/none-exist-path");
      fail();
    } catch (ErrnoException expected) {
      // Pass
    }
  }

  public void testReaddir() throws Exception {
    File current = tmp().get();
    File parent = current.getParentFile();
    File child1 = tmp().createDir("dir");
    File child2 = tmp().createFile("file");

    long dir = opendir(tmp().get().toString());
    try {

      Set<Dirent> expected = newHashSet(
          new Dirent(ino(current), DT_DIR, "."),
          new Dirent(ino(parent), DT_DIR, ".."),
          new Dirent(ino(child1), DT_DIR, child1.getName()),
          new Dirent(ino(child2), DT_REG, child2.getName()));

      Set<Dirent> actual = newHashSet(
          readdir(dir),
          readdir(dir),
          readdir(dir),
          readdir(dir));

      assertEquals(expected, actual);
      assertNull(readdir(dir));

    } finally {
      closedir(dir);
    }
  }

  private long ino(File file) throws ErrnoException {
    return stat(file.getPath()).getIno();
  }
}
