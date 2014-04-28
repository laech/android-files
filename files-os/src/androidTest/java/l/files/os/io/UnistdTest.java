package l.files.os.io;

import java.io.File;

import l.files.common.testing.FileBaseTest;
import l.files.os.ErrnoException;
import l.files.os.Unistd;

import static l.files.os.Unistd.F_OK;
import static l.files.os.Unistd.R_OK;
import static l.files.os.Unistd.W_OK;
import static l.files.os.Unistd.X_OK;
import static l.files.os.Unistd.readlink;
import static l.files.os.Unistd.symlink;

public final class UnistdTest extends FileBaseTest {

  public void testSymlink() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    assertFalse(b.exists());

    symlink(a.getPath(), b.getPath());

    assertTrue(b.exists());
    assertEquals(a.getCanonicalPath(), b.getCanonicalPath());
  }

  public void testReadlink() throws Exception {
    File a = tmp().createFile("a");
    File b = tmp().get("b");
    symlink(a.getPath(), b.getPath());
    assertEquals(a.getPath(), readlink(b.getPath()));
  }

  public void testAccess_R_OK_true() throws Exception {
    testAccessTrue(R_OK, new Consumer<File>() {
      @Override public void apply(File input) {
        assertTrue(input.setReadable(true));
      }
    });
  }

  public void testAccess_R_OK_false() throws Exception {
    testAccessFalse(R_OK, new Consumer<File>() {
      @Override public void apply(File input) {
        assertTrue(input.setReadable(false));
      }
    });
  }

  public void testAccess_W_OK_true() throws Exception {
    testAccessTrue(W_OK, new Consumer<File>() {
      @Override public void apply(File input) {
        assertTrue(input.setWritable(true));
      }
    });
  }

  public void testAccess_W_OK_false() throws Exception {
    testAccessFalse(W_OK, new Consumer<File>() {
      @Override public void apply(File input) {
        assertTrue(input.setWritable(false));
      }
    });
  }

  public void testAccess_X_OK_true() throws Exception {
    testAccessTrue(X_OK, new Consumer<File>() {
      @Override public void apply(File input) {
        assertTrue(input.setExecutable(true));
      }
    });
  }

  public void testAccess_X_OK_false() throws Exception {
    testAccessFalse(X_OK, new Consumer<File>() {
      @Override public void apply(File input) {
        assertTrue(input.setExecutable(false));
      }
    });
  }

  public void testAccess_F_OK_true() throws Exception {
    testAccessTrue(F_OK, new Consumer<File>() {
      @Override public void apply(File input) {}
    });
  }

  public void testAccess_F_OK_false() throws Exception {
    testAccessFalse(F_OK, new Consumer<File>() {
      @Override public void apply(File input) {
        assertTrue(input.delete());
      }
    });
  }

  public void testAccessTrue(int mode, Consumer<File> fn) throws Exception {
    File file = tmp().createFile("a");
    fn.apply(file);
    assertTrue(Unistd.access(file.getPath(), mode));
  }

  public void testAccessFalse(int mode, Consumer<File> fn) throws Exception {
    File file = tmp().createFile("a");
    fn.apply(file);
    try {
      Unistd.access(file.getPath(), mode);
      fail();
    } catch (ErrnoException e) {
      // Pass
    }
  }

  private static interface Consumer<T> {
    void apply(T input);
  }
}
