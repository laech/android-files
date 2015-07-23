package l.files.test;

import android.content.Intent;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.BaseActivityTest;
import l.files.features.objects.UiFileActivity;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ResourceException;
import l.files.fs.Visitor;
import l.files.fs.local.LocalResource;
import l.files.ui.browser.FilesActivity;

import static java.io.File.createTempFile;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;

public class BaseFilesActivityTest extends BaseActivityTest<FilesActivity> {

  private Resource dir;

  public BaseFilesActivityTest() {
    super(FilesActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = LocalResource.create(createTempDir());
    setActivityIntent(newIntent(dir));
  }

  private File createTempDir() throws IOException {
    File file = createTempFile("tmp", null);
    assertTrue(file.delete());
    assertTrue(file.mkdir());
    return file;
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    if (dir.exists(NOFOLLOW)) {
      dir.traverse(NOFOLLOW, setAllPermissions(), delete());
    }
  }

  private Visitor setAllPermissions() {
    return new Visitor() {
      @Override public Result accept(Resource resource) throws IOException {
        try {
          resource.setPermissions(Permission.all());
        } catch (ResourceException ignored) {
        }
        return CONTINUE;
      }
    };
  }

  private Visitor delete() {
    return new Visitor() {
      @Override public Result accept(Resource resource) throws IOException {
        resource.delete();
        return CONTINUE;
      }
    };
  }

  protected UiFileActivity screen() {
    return new UiFileActivity(getInstrumentation(), getActivity());
  }

  protected Resource dir() {
    return dir;
  }

  private Intent newIntent(Resource dir) {
    return new Intent().putExtra(EXTRA_DIRECTORY, dir);
  }
}
