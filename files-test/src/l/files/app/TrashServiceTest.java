package l.files.app;

import android.content.Intent;
import android.test.ServiceTestCase;
import l.files.test.TempDir;

import java.io.File;

import static java.lang.Thread.sleep;
import static l.files.app.TrashService.EXTRA_FILE;

public final class TrashServiceTest extends ServiceTestCase<TrashService> {

  private TempDir dir;

  public TrashServiceTest() {
    super(TrashService.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  public void testDeletesFile() throws Exception {
    File file = dir.newFile();
    assertTrue(file.exists());

    startService(newIntent(file));

    waitForFileToBeDeleted(file);
  }

  private Intent newIntent(File file) {
    return new Intent(getContext(), TrashService.class)
        .putExtra(EXTRA_FILE, file.getAbsolutePath());
  }

  private void waitForFileToBeDeleted(File file) throws InterruptedException {
    int count = 0;
    while (count < 10 && file.exists()) {
      sleep(10);
      ++count;
    }
    assertFalse(file.exists());
  }
}
