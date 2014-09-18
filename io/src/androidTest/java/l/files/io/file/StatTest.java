package l.files.io.file;

import com.google.common.base.Function;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.lang.System.currentTimeMillis;
import static l.files.io.file.ErrnoException.ENOENT;
import static org.assertj.core.api.Assertions.assertThat;

public final class StatTest extends FileBaseTest {

  public void testException() {
    try {
      Stat.stat("/not/exist");
      fail();
    } catch (ErrnoException e) {
      assertEquals(ENOENT, e.errno());
    }
  }

  public void testStat() throws Exception {
    testStatFile(new Function<File, Stat>() {
      @Override public Stat apply(File input) {
        try {
          return Stat.stat(input.getAbsolutePath());
        } catch (ErrnoException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  public void testLstat() throws Exception {
    testStatFile(new Function<File, Stat>() {
      @Override public Stat apply(File input) {
        try {
          return Stat.lstat(input.getAbsolutePath());
        } catch (ErrnoException e) {
          throw new AssertionError(e);
        }
      }
    });
  }

  private void testStatFile(Function<File, Stat> fn) throws IOException {
    long start = currentTimeMillis() / 1000;
    File file = tmp().createFile("test");
    write("hello", file, UTF_8);
    long end = currentTimeMillis() / 1000;

    Stat stat = fn.apply(file);

    assertNotNull(stat);
    assertThat(stat.size()).isEqualTo(file.length());
    assertThat(stat.nlink()).isEqualTo(1);
    assertThat(stat.atime()).isGreaterThanOrEqualTo(start);
    assertThat(stat.ctime()).isGreaterThanOrEqualTo(start);
    assertThat(stat.mtime()).isGreaterThanOrEqualTo(start);
    assertThat(stat.atime()).isGreaterThanOrEqualTo(end);
    assertThat(stat.ctime()).isGreaterThanOrEqualTo(end);
    assertThat(stat.mtime()).isGreaterThanOrEqualTo(end);
    assertThat(stat.ino()).isGreaterThan(0);

    // TODO more tests when there are more supporting test functions
  }
}
