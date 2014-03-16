package l.files.os.io;

import com.google.common.base.Function;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class OsTest extends BaseTest {

  private TempDir tmp;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tmp = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    tmp.delete();
  }

  public void testException(){
    try {
      Os.stat("/not/exist");
      fail();
    } catch (OsException e) {
      // Passed
    }
  }

  public void testSymlink() throws Exception {
    File a = tmp.createFile("a");
    File b = tmp.get("b");
    assertFalse(b.exists());

    Os.symlink(a.getPath(), b.getPath());

    assertTrue(b.exists());
    assertEquals(a.getCanonicalPath(), b.getCanonicalPath());
  }

  public void testStat() throws Exception {
    testStatFile(new Function<File, Stat>() {
      @Override public Stat apply(File input) {
        try {
          return Os.stat(input.getAbsolutePath());
        } catch (OsException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  public void testLstat() throws Exception {
    testStatFile(new Function<File, Stat>() {
      @Override public Stat apply(File input) {
        try {
          return Os.lstat(input.getAbsolutePath());
        } catch (OsException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  private void testStatFile(Function<File, Stat> fn) throws IOException {
    long start = MILLISECONDS.toSeconds(currentTimeMillis());
    File file = tmp.createFile("test");
    write("hello", file, UTF_8);
    long end = MILLISECONDS.toSeconds(currentTimeMillis());

    Stat stat = fn.apply(file);

    assertNotNull(stat);
    assertEquals(file.length(), stat.size);
    assertEquals(1, stat.nlink);
    assertTrue(stat.atime >= start);
    assertTrue(stat.ctime >= start);
    assertTrue(stat.mtime >= start);
    assertTrue(stat.atime >= end);
    assertTrue(stat.ctime >= end);
    assertTrue(stat.mtime >= end);
    assertTrue(stat.ino > 0);

    // TODO more tests when there are more supporting test functions
  }
}
