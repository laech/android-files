package l.files.test;

import static l.files.app.FilesActivity.EXTRA_DIR;

import android.content.Intent;
import java.io.File;
import l.files.app.FilesActivity;

public class BaseFilesActivityTest extends BaseActivityTest<FilesActivity> {

  private TempDir dir;

  public BaseFilesActivityTest() {
    super(FilesActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
    setActivityIntent(newIntent(dir.get()));
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  protected TempDir dir() {
    return dir;
  }

  private Intent newIntent(File dir) {
    return new Intent().putExtra(EXTRA_DIR, dir.getAbsolutePath());
  }
}
