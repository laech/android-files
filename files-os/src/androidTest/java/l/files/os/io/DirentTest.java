package l.files.os.io;

import java.io.File;
import java.util.Set;

import l.files.common.testing.FileBaseTest;
import l.files.os.Dirent;
import l.files.os.ErrnoException;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.os.Dirent.DT_DIR;
import static l.files.os.Dirent.DT_REG;
import static l.files.os.Dirent.closedir;
import static l.files.os.Dirent.opendir;
import static l.files.os.Dirent.readdir;
import static l.files.os.Stat.stat;

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
          Dirent.create(ino(current), DT_DIR, "."),
          Dirent.create(ino(parent), DT_DIR, ".."),
          Dirent.create(ino(child1), DT_DIR, child1.getName()),
          Dirent.create(ino(child2), DT_REG, child2.getName()));

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
    return stat(file.getPath()).ino;
  }
}
