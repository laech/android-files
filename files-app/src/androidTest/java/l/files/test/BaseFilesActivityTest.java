package l.files.test;

import android.content.Intent;

import java.io.File;

import l.files.ui.FilesActivity;
import l.files.common.testing.BaseActivityTest;
import l.files.common.testing.TempDir;
import l.files.features.object.UiFileActivity;

import static l.files.ui.FilesActivity.EXTRA_DIRECTORY;
import static l.files.provider.FilesContract.getFileId;

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
    super.tearDown();
    dir.delete();
  }

  protected final UiFileActivity screen() {
    return new UiFileActivity(getInstrumentation(), getActivity());
  }

  protected final TempDir dir() {
    return dir;
  }

  private Intent newIntent(File dir) {
    return new Intent().putExtra(EXTRA_DIRECTORY, getFileId(dir));
  }
}
