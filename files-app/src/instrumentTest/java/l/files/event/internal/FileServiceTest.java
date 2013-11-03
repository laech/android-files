package l.files.event.internal;

import static android.os.SystemClock.sleep;

import android.test.ServiceTestCase;
import java.io.File;
import l.files.test.TempDir;

public abstract class FileServiceTest<T extends FileService> extends ServiceTestCase<T> {

  protected TempDir dir;

  public FileServiceTest(Class<T> serviceClass) {
    super(serviceClass);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  protected void waitForNonExistence(File file) {
    for (int count = 0; count < 10 && file.exists(); ++count) {
      sleep(10);
    }
    assertFalse(file.exists());
  }

  protected void waitForExistence(File file) {
    for (int count = 0; count < 10 && !file.exists(); ++count) {
      sleep(10);
    }
    assertTrue(file.exists());
  }
}
