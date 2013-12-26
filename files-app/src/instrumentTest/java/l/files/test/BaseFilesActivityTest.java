package l.files.test;

import static l.files.app.FilesActivity.EXTRA_DIRECTORY;

import android.content.Intent;
import java.io.File;
import l.files.app.FilesActivity;
import l.files.features.object.UiFileActivity;

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

  protected final UiFileActivity screen() {
    return new UiFileActivity(getInstrumentation(), getActivity());
  }

  protected final TempDir dir() {
    return dir;
  }

  private Intent newIntent(File dir) {
    return new Intent().putExtra(EXTRA_DIRECTORY, dir.getAbsolutePath());
  }
}
